/*
 * Copyright (C) 2019 Jean-Jacques Ponciano
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

import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.aee.memory.Memory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.mgt.Explain;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.util.FileManager;

/**
 * Kee for owl file
 *
 * @author Jean-Jacques Ponciano
 */
public class KeeOwlFile extends Kee {

    protected String workingOntPath;
    public OntModel workingModel;

    public KeeOwlFile(String ontologyPath, String workingDir) throws IOException, FileNotFoundException, KnowdipException {
        super(ontologyPath, workingDir);
        String name = workingDir + new File(ontologyPath).getName();
        this.workingOntPath = name.substring(0, name.lastIndexOf('.')) + "KD.owl";
        File file = new File(workingOntPath);
        //creates the file if it not exists
        if (!file.exists()) {
            Files.copy(new File(ontologyPath).toPath(), file.toPath(), REPLACE_EXISTING);

        }
        this.workingModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, FileManager.get()
                .loadModel(this.workingOntPath));

    }

    @Override
    public synchronized ResultSet select(String queryString) {
        queryString = this.prefix + queryString;
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, this.getWorkingModel());
        queryExecution.getContext().set(ARQ.symLogExec, Explain.InfoLevel.NONE);

        return queryExecution.execSelect();
    }

    public String selectAsText(String query) {
        return ResultSetFormatter.asText(this.select(query), new Prologue(this.workingModel));
    }

    protected synchronized boolean hasNext(ResultSet select) {
        return select.hasNext();
    }

    @Override
    protected Model execConstruct(String queryString) throws KnowdipException {

        queryString = prefix + queryString;
        String removeGraph = removeGraph(queryString);
        Query query = QueryFactory.create(removeGraph);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, this.getWorkingModel());
        queryExecution.getContext().set(ARQ.symLogExec, Explain.InfoLevel.NONE);

        return queryExecution.execConstruct();
    }

    @Override
    public synchronized void update(String query) {
        try {
            query = this.prefix + query;
            String res = removeGraph(query);
            UpdateAction.parseExecute(res, this.workingModel);
        } catch (KnowdipException ex) {
            Logger.getLogger(KeeOwlFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String removeGraph(String query) throws KnowdipException {
        String res;
        if (query.contains("GRAPH")) {
            res = query.replaceAll("\n*\\s*GRAPH\\s<.*?>\\s*[{]", "");
            int lastIndexOf = res.lastIndexOf("}");
            res = res.substring(0, lastIndexOf);
        } else {
            res = query;
        }
        if (res.contains("GRAPH")) {
            throw new KnowdipException("Querry with graph in owl file: " + res);
        }
        return res;
    }

    @Override
    public synchronized void close() {
        try {
            saveOntology();
            Memory.get().write(this.memoryPath);
        } catch (IOException ex) {
            Logger.getLogger(KeeOwlFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void saveOntology() throws IOException {
        this.workingModel.write(new FileWriter(this.workingOntPath));
    }

    @Override
    public OntModel getWorkingModel() {
        return workingModel;
    }

}
