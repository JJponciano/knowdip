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

import info.ponciano.lab.knowdip.KD;
import info.ponciano.lab.knowdip.aee.AlgorithmRegistry;
import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.Algorithm;
import info.ponciano.lab.knowdip.aee.memory.Memory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.util.FileManager;

/**
 * Knowdip execution engine
 *
 * @author Jean-Jacques ponciano
 */
public abstract class Kee {

    protected final String ontologyPath;
    protected final String datasetPath;
    protected AlgorithmRegistry algorithmRegistry;
    protected String memoryPath;
    protected OntModel model;
    protected String prefix;
    protected Memory memory;

    /**
     * Create new instance of @code{Controler}
     *
     * @param ontologyPath path of the owl file
     * @param workingDir working directory
     * @throws IOException if at least one of the both file are wrong. Example
     * of parameters setting in knowdip:      <code>
     * KD.get().getWorkspaceOntology() + "knowdip-1.3.owl,
     * KD.get().getWorkspace() + "dataset/";
     * </code>
     * @throws java.io.FileNotFoundException
     * @throws info.ponciano.lab.knowdip.KnowdipException
     * @throws info.ponciano.lab.semcv.MemoryException if the folder does not
     * contains the memory data.
     */
    public Kee(String ontologyPath, String workingDir) throws IOException, FileNotFoundException, KnowdipException {
        this.memory = new Memory();
        new File(workingDir).mkdirs();
        this.ontologyPath = ontologyPath;
        this.datasetPath = workingDir + "dataset/";
        this.memoryPath = workingDir + "kmemory";
        if (new File(memoryPath).exists()) {
            this.memory.read(memoryPath);
        }
        this.init();
        this.prefix = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
        this.prefix += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
        this.prefix += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
        this.prefix += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
        this.prefix += "PREFIX knowdip: <" + KD.NS + ">\n";
    }

    public Memory getMemory() {
        return this.memory;
    }

    /**
     * Add new algorithm to be registered
     *
     * @param algo algorithm to be registered.
     * @throws info.ponciano.lab.knowdip.KnowdipException
     */
    public void add(Class<? extends Algorithm> algo) throws KnowdipException {
        this.algorithmRegistry.register(algo);
    }

    /**
     * Data set initialization from an existing Knowdip ontology file.
     *
     * @throws FileNotFoundException if ontology file not present.
     */
    private void init() throws FileNotFoundException, IOException, KnowdipException {
        /*
        * Loading ontology from filee.
         */
        this.model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, FileManager.get()
                .loadModel(this.ontologyPath));

        this.algorithmRegistry = new AlgorithmRegistry();
        this.algorithmRegistry.load(model);
       
    }

    /**
     * Inference performed using the Jena OWL reasoner.
     *
     * @throws info.ponciano.lab.knowdip.KnowdipException if the model is
     * inconsistent.
     */
    public void infer() throws KnowdipException {
        Model wm = getWorkingModel();
        final Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        final InfModel infModel = ModelFactory.createInfModel(reasoner, wm);
        infModel.prepare();

        ValidityReport validity = infModel.validate();
        if (!validity.isValid()) {
            throw new KnowdipException("Inconsistencies found!");
        }
        wm.add(infModel.getDeductionsModel());
    }

    /**
     * Execute a select query on the dataset
     *
     * @param queryString query to be executed
     * @return the resultset of the query
     */
    public abstract ResultSet select(String queryString);

    /**
     * Execute a update query on the dataset
     *
     * @param query query to be executed
     * @throws info.ponciano.lab.knowdip.KnowdipException if something wrong
     */
    public abstract void update(String query) throws KnowdipException;

    public synchronized boolean construct(String queryString) throws KnowdipException {
        Model wm = getWorkingModel();
        Model execConstruct = this.execConstruct(queryString);
        if (execConstruct == null || execConstruct.isEmpty() || wm.containsAll(execConstruct)) {
            return false;
        }
        wm.add(execConstruct);
        return true;
    }

    /**
     * Export all point cloud in txt file format.
     *
     * @throws IOException
     */
    public void saveMemory() throws IOException {
        this.memory.write(memoryPath);
    }

    /**
     * Clean
     *
     * @return
     */
//    public void clearMemory() {
//        try {
//            Memory.get().write(memoryPath);
//              Memory.get().clear();
//        } catch (IOException ex) {
//            Logger.getLogger(Kee.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    /**
     * get the memory path
     *
     * @return the path of the knowdip memory folder.
     */
    public String getMemoryPath() {
        return memoryPath;
    }

    /**
     * Closes and saves all work.
     */
    public abstract void close();

    /**
     * get the working model
     *
     * @return the actual working model.
     */
    protected abstract Model getWorkingModel();

    /**
     * Parse <code>ResultSet</code> into <code> List</code>
     *
     * @author Jean-Jacques Ponciano
     * @param resultset ResultSet to be parsed
     * @param key String use to extract result in the result set.
     * @return list created
     * @throws KnowdipException If no elements are found
     */
    public List<String> toStringResultList(ResultSet resultset, String key) throws KnowdipException {
        List<String> res = new ArrayList<>();
        while (resultset.hasNext()) {
            QuerySolution result = resultset.next();
            Resource rs = result.getResource(key);
            if (rs == null) {
                throw new KnowdipException("no \"" + key + "\" found in the result set " + result.varNames());
            }
            res.add(rs.getURI());
        }
        return res;
    }
//
//    private void inferPellet() throws KnowdipException {
//        //System.out.println("- Inferring added data...");
///*
//        
//           OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RULE_INF);
//            OntClass data = m.createClass(KD.NS + "Data");
//            OntClass file = m.createClass(KD.NS + "File");
//            data.addSubClass(file);
//            m.createIndividual(KD.NS + "test", file);
//            m.write(new FileWriter(ontologyPath));
//         */
//        //Reasoner reasoner = PelletReasonerFactory.theInstance().create();
//        Model schema = FileManager.get().loadModel(ontologyPath);
//        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
//        reasoner = reasoner.bindSchema(schema);
//
//        InfModel infModel = ModelFactory.createInfModel(reasoner, dataset.getNamedModel(KD.URI));
//
//        /*
//		 * To extract the model, a transaction must be open in READ mode.
//         */
////        dataset.begin(ReadWrite.READ);
////        ModelExtractor me = new ModelExtractor(infModel);
////
////        dataset.end();
//        // System.out.println("- Replacing current data set model <" + KD.URI + "> with the inferred model...");
//
//        /*
//		 * To replace a currently existing named model within the dataset, a transaction must be open in WRITE mode.
//         */
//        dataset.begin(ReadWrite.WRITE);
//
//        dataset.replaceNamedModel(KD.URI, infModel);
//        dataset.commit();
//        dataset.end();
//    }

    /**
     * Execute a construct query.
     *
     * @param queryString query to be processed.
     * @return the model to be added to the working model.
     * @throws info.ponciano.lab.knowdip.KnowdipException if something wrong.
     */
    protected abstract Model execConstruct(String queryString) throws KnowdipException;

    /**
     * Get the ontology model
     *
     * @return
     */
    public OntModel getModel() {
        return this.model;
    }

    public String getOntologyPath() {
        return ontologyPath;
    }

}
