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
import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.Algorithm;
import info.ponciano.lab.knowdip.aee.memory.Memory;
import info.ponciano.lab.knowdip.reasoner.automatic.PiRegex;
import info.ponciano.lab.knowdip.reasoner.automatic.SemObject;
import info.ponciano.lab.knowdip.reasoner.automatic.algorithms.SparqlAlgorithm;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

/**
 *
 * @author Jean-Jacques Ponciano
 */
public class KReasonerOwlFile extends KReasoner {

    protected PiOnt piont;
    protected List<OntClass> objects;
    protected Kee kee;

    public KReasonerOwlFile(String ontologyPath, String workingDir) throws FileNotFoundException, PiOntologyException, IOException, KnowdipException {
        this.kee = new KeeOwlFile(ontologyPath, workingDir);
        this.piont = new PiOnt(this.kee.getWorkingModel());
        //select every subclass of object
        OntClass objectCl = piont.getOntClass(KD.OBJECT);
        this.objects = piont.getSubClass(objectCl, true);
    }

    /**
     * Execute algorithms by translate algorithms information from ontology to
     * SPARQL queries
     *
     * @throws KnowdipException
     * @deprecated it is more robust to use @code{interprets()}
     * @see KReasoner.interprets
     */
    public void genericExecution() throws KnowdipException {
        try {
            //get the algorithm class
            OntClass algoOC = piont.getOntClass(KD.ALGORITHM);
            //get all subclass of algorithm
            List<OntClass> algos = piont.getSubClass(algoOC, true);
            //for each algorithm's subclass
            boolean construct = true;
            while (construct) {
                this.inferRoot();//infer the ontology with root rules
                construct = false;
                for (OntClass algo : algos) {
                    //build semantically the algorithm
                    SparqlAlgorithm semAlgo = new SparqlAlgorithm(algo, piont);
                    //get update if it exists
                    List<String> updateQuery = getUpdateQuery(semAlgo);
                    construct |= !updateQuery.isEmpty();
                    updateQuery.forEach((query) -> {

                        try {
                            this.kee.update(query);
                        } catch (KnowdipException ex) {
                            Logger.getLogger(KReasonerOwlFile.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                }
            }
        } catch (PiOntologyException ex) {
            Logger.getLogger(KReasonerOwlFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Extract SParql construct from object description inside the ontology. Do
     * not yet support OWL
     *
     * @deprecated better to use direct SPARQL CONSTRUCT;
     * @throws KnowdipException
     * @throws PiOntologyException
     */
    public void genericClassification() throws KnowdipException, PiOntologyException {
        //for each class and up to idempotence
        boolean construct = true;
        while (construct) {
            construct = false;
            //for each object, get all restriction
            for (OntClass o : objects) {
                //build semantically the object
                SemObject semObject = new SemObject(o, piont);
                //select every individual corresponding to the restriction and
                //classify individuals as the object
                String classifQuery = semObject.getClassif();
                if (!classifQuery.isEmpty()) {
//                    System.out.println("***************");
//                    System.out.println(classifQuery);
                    construct |= this.kee.construct(classifQuery);
                }
            }
        }
    }

    private List<String> getUpdateQuery(SparqlAlgorithm semAlgo) throws KnowdipException {

        String selectQuery = semAlgo.getSelect();
        String selectOut = semAlgo.getExec();

        String maj = semAlgo.getInsert();
//        System.out.println("---------------------------------------------\n"
//                + selectQuery + "*********************\n"
//                + selectOut + "*********************\n"
//                + "\n" + maj);

//        System.out.println("vars: ");
        List<String> vars = semAlgo.getVarSelected();

        return getInsert(selectQuery, vars, selectOut, maj);
    }

    public PiOnt getPiont() {
        return piont;
    }

    @Override
    protected Kee getKee() {
        return this.kee;
    }
}
