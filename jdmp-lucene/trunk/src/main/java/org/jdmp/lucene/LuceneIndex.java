/*
 * Copyright (C) 2008-2009 Holger Arndt, A. Naegele and M. Bundschus
 *
 * This file is part of the Java Data Mining Package (JDMP).
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * JDMP is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JDMP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with JDMP; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package org.jdmp.lucene;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractListModel;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jdmp.core.algorithm.Algorithm;
import org.jdmp.core.algorithm.index.AbstractIndex;
import org.jdmp.core.algorithm.index.Index;
import org.jdmp.core.algorithm.index.MultiIndex;
import org.jdmp.core.dataset.DataSet;
import org.jdmp.core.dataset.DefaultDataSet;
import org.jdmp.core.sample.HasSampleList;
import org.jdmp.core.sample.Sample;
import org.jdmp.core.util.ObservableList;
import org.jdmp.core.variable.Variable;
import org.ujmp.core.Matrix;
import org.ujmp.core.interfaces.Erasable;
import org.ujmp.core.util.MathUtil;
import org.ujmp.core.util.SerializationUtil;
import org.ujmp.core.util.io.FileUtil;

public class LuceneIndex extends AbstractIndex implements Flushable, Closeable,
		Erasable, HasSampleList {
	private static final long serialVersionUID = -8483996550983833243L;

	private IndexWriter indexWriter = null;

	private IndexSearcher indexSearcher = null;

	private ThreadPoolExecutor executor = null;

	private final Set<String> fields = new HashSet<String>();

	private Directory directory = null;

	private File path = null;

	private long runningId = 0;

	private final Analyzer analyzer = new StandardAnalyzer();

	private boolean readOnly = true;

	public LuceneIndex(Index index) throws Exception {
		this(null, false, new Index[] { index });
	}

	public LuceneIndex(Index... indices) throws Exception {
		this(null, false, indices);
	}

	public LuceneIndex(File path, Index... indices) throws Exception {
		this(path, false, indices);
	}

	public LuceneIndex(File path, boolean readOnly, Index... indices)
			throws Exception {
		this.readOnly = readOnly;

		if (indices.length == 1) {
			getAlgorithms().put("Index0", (Algorithm) indices[0]);
		} else if (indices.length > 1) {
			MultiIndex multiIndex = new MultiIndex(indices);
			getAlgorithms().put("Index0", multiIndex);
		}

		if (path == null) {
			path = File.createTempFile("lucene", "");
			path.delete();
			path.mkdir();
			path.deleteOnExit();
			readOnly = false;
		}

		fields.add(Sample.ID);

		this.path = path;

		directory = FSDirectory.getDirectory(path);

		if (IndexReader.indexExists(directory)) {
			if (!readOnly) {
				if (IndexWriter.isLocked(directory)) {
					IndexWriter.unlock(directory);
				}
				indexWriter = new IndexWriter(directory, analyzer,
						MaxFieldLength.UNLIMITED);
			}

			prepareReader();
			Collection<?> c = indexSearcher.getIndexReader().getFieldNames(
					FieldOption.ALL);
			for (Object o : c) {
				fields.add((String) o);
			}

		} else if (!readOnly) {
			indexWriter = new IndexWriter(directory, analyzer, true,
					MaxFieldLength.UNLIMITED);
		}

		setSamples(new LuceneSampleList(this));

		executor = new ThreadPoolExecutor(0, 1, 1000L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	@Override
	public synchronized void add(Sample sample) throws Exception {
		if (readOnly || sample == null) {
			return;
		}

		prepareWriter();
		Document doc = new Document();

		String id = sample.getId();
		if (id == null) {
			id = "" + runningId++;
		}

		doc.add(new Field(Sample.ID, id, Store.COMPRESS,
				Field.Index.NOT_ANALYZED));

		for (Variable v : sample.getVariables()) {
			String key = v.getLabel();
			if (!Sample.ID.equals(key)) {
				String value = "";
				for (Matrix m : v.getMatrixList()) {
					for (long[] c : m.availableCoordinates()) {
						value += " " + m.getAsString(c);
					}
				}
				doc.add(new Field(key, value.trim(), Store.COMPRESS,
						Field.Index.ANALYZED));
				fields.add(key);
			}
		}

		doc.add(new Field("RawData", SerializationUtil.serialize(sample),
				Store.COMPRESS));

		indexWriter.updateDocument(new Term(Sample.ID, id), doc);
	}

	public int getSize() {
		try {
			if (indexWriter != null) {
				return indexWriter.maxDoc();
			} else {
				prepareReader();
				return indexSearcher.maxDoc();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public synchronized Sample getSampleAt(int index) throws Exception {
		prepareReader();
		if (indexSearcher.getIndexReader().isDeleted(index)) {
			return null;
		} else {
			Document doc = indexSearcher.doc(index);
			Sample s = (Sample) SerializationUtil.deserialize(doc
					.getBinaryValue("rawdata"));
			return s;
		}
	}

	public synchronized Sample getSample(String id) throws Exception {
		DataSet ds = search("Id:" + id);
		if (ds != null && !ds.getSamples().isEmpty()) {
			return ds.getSamples().getElementAt(0);
		}
		return null;
	}

	public synchronized DataSet search(String query) throws Exception {
		if (executor.getQueue().size() < 1000) {
			executor.submit(new SearchCallable(query));
		}

		String[] fs = new String[fields.size()];
		MultiFieldQueryParser p = new MultiFieldQueryParser(fields.toArray(fs),
				analyzer);
		Query q = null;
		if (query == null || "".equals(query)) {
			q = p.parse("*");
		} else if ("*".equals(query)) {
			q = p.parse("*");
		} else {
			q = p.parse(query);
		}
		return search(q, 100);
	}

	public synchronized DataSet search(Query query, int count) throws Exception {
		prepareReader();
		TopDocs td = indexSearcher.search(query, count);
		DataSet result = new DefaultDataSet();
		for (ScoreDoc sd : td.scoreDocs) {
			int id = sd.doc;
			Document doc = indexSearcher.doc(id);
			Sample s = (Sample) SerializationUtil.deserialize(doc
					.getBinaryValue("RawData"));
			s.setMatrix(Sample.SCORE, MathUtil.getMatrix(sd.score));
			result.getSamples().add(s);
		}
		return result;
	}

	private synchronized void prepareReader() throws CorruptIndexException,
			IOException {
		if (indexWriter != null) {
			indexWriter.commit();
		}
		if (indexSearcher != null
				&& !indexSearcher.getIndexReader().isCurrent()) {
			indexSearcher.close();
			indexSearcher = null;
		}
		if (indexSearcher == null) {
			indexSearcher = new IndexSearcher(directory);
		}
	}

	private synchronized void prepareWriter() throws IOException {
		if (indexSearcher != null) {
			indexSearcher.close();
			indexSearcher = null;
		}
	}

	@Override
	public synchronized void flush() throws IOException {
		if (indexWriter != null) {
			indexWriter.commit();
		}
	}

	public synchronized void optimize() throws IOException {
		if (indexWriter != null) {
			indexWriter.optimize();
		}
	}

	@Override
	public synchronized void close() throws IOException {
		if (indexSearcher != null) {
			indexSearcher.close();
			indexSearcher = null;
		}
		if (indexWriter != null) {
			indexWriter.close();
		}
	}

	@Override
	public synchronized void erase() throws IOException {
		if (readOnly) {
			return;
		}
		close();
		FileUtil.deleteRecursive(path);
	}

	@Override
	public ObservableList<Sample> getSamples() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSamples(ObservableList<Sample> samples) {
		// TODO Auto-generated method stub

	}

	class LuceneSampleList extends AbstractListModel implements
			ObservableList<Sample> {
		private static final long serialVersionUID = -7189321183317113764L;

		private LuceneIndex index = null;

		public LuceneSampleList(LuceneIndex index) {
			this.index = index;
		}

		@Override
		public void add(Sample sample) {
			try {
				index.add(sample);
				fireContentsChanged();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void addAll(Collection<Sample> values) {
			throw new RuntimeException("not implemented");
		}

		@Override
		public boolean remove(Sample value) {
			throw new RuntimeException("not implemented");
		}

		@Override
		public void clear() {
			throw new RuntimeException("not implemented");
		}

		@Override
		public Sample getElementAt(int i) {
			try {
				return index.getSampleAt(i);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public void fireContentsChanged() {
			fireContentsChanged(this, -1, -1);
		}

		@Override
		public int indexOf(Sample value) {
			throw new RuntimeException("not implemented");
		}

		@Override
		public boolean isEmpty() {
			return getSize() == 0;
		}

		@Override
		public Collection<Sample> toCollection() {
			throw new RuntimeException("not implemented");
		}

		@Override
		public int getSize() {
			try {
				return index.getSize();
			} catch (Exception e) {
				e.printStackTrace();
				return 0;
			}
		}

		@Override
		public Iterator<Sample> iterator() {
			throw new RuntimeException("not implemented");
		}

	}

	class SearchCallable implements Callable<Object> {

		private String query = null;

		public SearchCallable(String query) {
			this.query = query;
		}

		@Override
		public Object call() throws Exception {
			for (Algorithm a : getAlgorithms()) {
				try {
					if (a instanceof Index) {
						System.out.println("searching for " + query + " in "
								+ a);
						DataSet ds = ((Index) a).search(query);
						if (ds != null) {
							add(ds);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

	}
}
