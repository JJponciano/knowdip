/*
 * Copyright (C) 2020 Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package info.ponciano.lab.knowdip.reasoner;

import info.ponciano.lab.knowdip.KD;
import info.ponciano.lab.knowdip.Knowdip;
import info.ponciano.lab.knowdip.aee.KnowdipException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateAction;

/**
 *
 * @author Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
 */
public class KeeTS extends Kee {

    protected Dataset dataset;

    public KeeTS(String ontologyPath, String workingDir) throws IOException, FileNotFoundException, KnowdipException {
        super(ontologyPath, workingDir);
        this.init();
    }

    private void init() throws FileNotFoundException, IOException {
        System.out.println("- Creating the dataset...");
        File datasetDirectory = new File(datasetPath);
        datasetDirectory.mkdir();
        dataset = TDBFactory.createDataset(datasetDirectory.getAbsolutePath());
        dataset.begin(ReadWrite.WRITE);
        dataset.addNamedModel(KD.URI, model);
        dataset.commit();
        dataset.end();
    }

    @Override
    protected OntModel getWorkingModel() {
        OntModel createOntologyModel = ModelFactory.createOntologyModel(
                OntModelSpec.OWL_MEM, dataset.getNamedModel(KD.URI));
        return createOntologyModel;
    }

    @Override
    public void infer() {
        dataset.begin(ReadWrite.WRITE);
        try {
            super.infer();
            dataset.commit();
        } catch (final KnowdipException e) {
            dataset.abort();
        } finally {
            dataset.end();
        }
    }

    public Dataset getDataset() {
        return dataset;
    }

    @Override
    public Iterator<KSolution> select(String queryString) {
        dataset.begin(ReadWrite.READ);
        queryString = this.prefix + queryString;
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, getWorkingModel());
        ResultSet resultSet = queryExecution.execSelect();
        Iterator<KSolution> iterator = Knowdip.getIterator(queryString, resultSet);
        dataset.end();
        return iterator;
    }

    @Override
    protected Model execConstruct(String queryString) throws KnowdipException {
        queryString = this.prefix + queryString;
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, this.getWorkingModel());
        return queryExecution.execConstruct();
    }

    /**
     * Execute a update query on the dataset
     *
     * @param query query to be executed
     */
    @Override
    public void update(String query) {
        int insertPart = query.indexOf('{')+1;
        query = query.substring(0, insertPart) + "\nGRAPH <" + KD.URI + "> {\n" + query.substring(insertPart, query.length()) + "}";
        query = this.prefix + query;
        dataset.begin(ReadWrite.WRITE);
        UpdateAction.parseExecute(query, dataset);
        dataset.commit();
        dataset.end();
    }

    @Override
    public void close() {
        try {
            this.dataset.close();
            this.saveMemory();
        } catch (IOException ex) {
            Logger.getLogger(KeeTS.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String selectAsText(String queryString) {
        dataset.begin(ReadWrite.READ);
        queryString = this.prefix + queryString;
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, getWorkingModel());
        ResultSet resultSet = queryExecution.execSelect();
        String asText = ResultSetFormatter.asText(resultSet, new Prologue(this.getWorkingModel()));
        dataset.end();
        return asText;

    }
}
