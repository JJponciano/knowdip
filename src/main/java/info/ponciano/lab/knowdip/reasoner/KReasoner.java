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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

/**
 *
 * @author Jean-Jacques Ponciano
 */
public class KReasoner extends KeeOwlFile {

    protected PiOnt piont;
    protected List<OntClass> objects;

    public KReasoner(String ontologyPath, String datasetPath) throws IOException, KnowdipException, FileNotFoundException, PiOntologyException {
        super(ontologyPath, datasetPath);

        this.piont = new PiOnt(workingOntPath);
        //select every subclass of object
        OntClass objectCl = piont.getOntClass(KD.OBJECT);
        this.objects = piont.getSubClass(objectCl, true);

    }

    private void inferRoot() throws KnowdipException {
        this.construct("CONSTRUCT { ?x rdf:type ?sub } WHERE {"
                + "?x rdf:type ?t . ?t rdfs:subClassOf ?sub . "
                + "} ");
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

                        this.update(query);
                    });
                }
            }
        } catch (PiOntologyException ex) {
            Logger.getLogger(KReasoner.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Interpret sparql queries to execute algorithms add enrich the ontology
     *
     * @param select Query to select every usefull variable
     * @param out Query to execute algorithms using same variable code than two
     * other queries.
     * @param insert Query to enrich the ontology using same variable code than
     * two other queries.
     * @return True if queries produces results, false otherwise
     */
    public boolean interprets(String select, String out, String insert) {
        List<String> vars = getSparqlVar(select);
        List<String> updateQuery = this.getInsert(select, vars, out, insert);
        if (updateQuery.isEmpty()) {
            return false;
        }
        updateQuery.forEach((query) -> {
//                 System.out.println(query);
            this.update(query);
        });
        return true;
    }

    public static List<String> getSparqlVar(String select) {
        String expression = "(\\?\\S+)";
        Pattern pattern = Pattern.compile(expression);
        List<String> res = new ArrayList<>();
        Matcher matcher = pattern.matcher(select);
        while (matcher.find()) {
            final String group = matcher.group();
            if (!res.contains(group)) {
                res.add(group);
            }
        }
        return res;
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
                    construct |= this.construct(classifQuery);
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

    private List<String> getInsert(String selectQuery, List<String> vars, String selectOut, String maj) {
        List<String> updateQuery = new LinkedList<>();
        ResultSet select = this.select(selectQuery);
        //select all the variables needed

        List<String> executeMemory = new LinkedList<>();
        //get every elements
        while (select.hasNext()) {
            QuerySolution next = select.next();
            //build var values
            /// List<VarRDFNode> values = new ArrayList<>();
            HashMap<String, String> varNode = new HashMap<>();
            vars.forEach((v) -> {
                RDFNode get = next.get(v);
                if (get != null) {
                    varNode.put(v, this.getV(get));
                }
            });
            String sout = "" + selectOut;
            String updateq = "" + maj;
            //replace every var in the execute query  by its  value
            for (String key : varNode.keySet()) {
                sout = this.replaceAllString(sout, key, varNode.get(key));
                updateq = this.replaceAllString(updateq, key, varNode.get(key));
            }
            //Executes algorithms 
            if (!executeMemory.contains(sout)) {
                executeMemory.add(sout);
                ResultSet selectOutrs = this.select(sout);
                //retrieve the result.
                while (selectOutrs.hasNext()) {
                    QuerySolution nextOut = selectOutrs.nextSolution();
                    String out = getV(nextOut.get("?out"));

                    /*if the data to be added is not a string
                    if (out.contains("^")) {
                        if (out.contains("#string")) {
                            System.out.println(out);
                            out  = out.replaceAll("\\^.*#string", "");
                                  System.out.println("************************");
                                     System.out.println(out);
                        } else {
                            PiString piString = new PiString(out);
                            piString.removeAfter('^');
                            piString.remove("\"");
                            piString.remove("^");
                            piString.removeStartingSpace();
                            out = piString.toString();
                        }
                    }*/
                    String upOut = this.replaceAllString(updateq, "?out", out);
                    //update the knowledge base
                    //  System.out.println(upOut);
                    updateQuery.add(upOut);
                }
            }
        }
        return updateQuery;
    }

    private String getV(RDFNode get) {
        String result;
        if (get.isResource()) {
            result = "<" + get.asResource().getURI() + ">";
        } else if (get.isLiteral()) {
            Literal lit = get.asLiteral();
            RDFDatatype datatype = lit.getDatatype();
            String uri = datatype.getURI();
            result = " \"" + lit.getLexicalForm() + "\"^^" + uri.replaceAll("http://www.w3.org/2001/XMLSchema#", "xsd:");
        } else {
            result = get.toString();
        }
        return result;
    }

    /**
     * Executes construct with the execution of an algorithms.
     *
     * The output of the algorithms should has the name "?out".
     *
     * @param construct consctruct with asking the execution of an algorithms
     * @return true if queries produces results, false otherwise
     */
    public boolean interprets(String construct) {
        String[] sea = KReasoner.splitToExecuteAlgo(construct);
        return this.interprets(sea[0], sea[1], sea[2]);

    }

    protected static String[] splitToExecuteAlgo(String construct) {

        String[] select = new String[3];//0->select 1-> out 2-> insert
        PiRegex reg = new PiRegex("CONSTRUCT\\{.*?\\}", construct);
        select[2] = "INSERT DATA" + reg.getFirst().substring(9);

        reg = new PiRegex("WHERE\\{.*?\\?out", construct);
        String first = reg.getFirst();
        select[0] = first.substring(0, first.length() - 4) + "}";
        String temp = select[0].replaceAll("FILTER NOT EXISTS\\s*\\{.*?\\}", " ");
        List<String> vars = getSparqlVar(temp);

        vars.forEach((var) -> {
            select[0] = var + " " + select[0];
        });
        select[0] = "SELECT " + select[0];

        reg = new PiRegex("\\?out\\sknowdip:\\w+\\(.*?\\)", construct);
        select[1] = "SELECT ?out WHERE{" + reg.getFirst() + "}";
        /*"SELECT ?v0 ?i0  WHERE {"
                + "?i0 rdf:type knowdip:File ."
                + "FILTER NOT EXISTS { ?i0 rdf:type knowdip:Nothing. } "
                + "FILTER NOT EXISTS { ?something knowdip:readFrom ?i0 } . "
                + "?i0 knowdip:hasSource ?v0 .  }";
        String out = "SELECT ?out  WHERE{ ?out knowdip:LoadCloud( \"hasSource =\" ?v0) }";

        String insert = "INSERT DATA {  ?out rdf:type knowdip:FullPointCloud . ?out knowdip:readFrom ?i0. }";

        String construct = "CONSTRUCT{ ?out rdf:type knowdip:FullPointCloud . ?out knowdip:readFrom ?i0.} "
                + "WHERE{"
                + "?i0 rdf:type knowdip:File ."
                + "FILTER NOT EXISTS { ?i0 rdf:type knowdip:Nothing. } "
                + "FILTER NOT EXISTS { ?something knowdip:readFrom ?i0 } . "
                + "?i0 knowdip:hasSource ?v0 ."
                + "?out knowdip:LoadCloud( \"hasSource =\" ?v0)"
                + "}";*/
        return select;

    }

    /**
     * Remove every individual corresponding to the variable specify after the
     * {@code REMOVE}.
     * <p>
     * For example for removing every dog: {@code "REMOVE ?c WHERE{ ?c rdf:type dog
     * } } </p>
     *
     * @param query Query to be interpreted
     */
    public void remove(String query) {
        PiRegex reg = new PiRegex("REMOVE\\s*(.*?)[\\s|{]", query);
        String var = reg.getGroup(1).get(0);
        query = query.replaceFirst("REMOVE|remove", "SELECT");

        ResultSet select = this.select(query);
        List<String> uris = new ArrayList();
        while (select.hasNext()) {
            QuerySolution next = select.next();
            var get = next.get(var);
            uris.add(get.asResource().getURI());
        }
        for (String uri : uris) {
            this.update("DELETE WHERE {<" + uri + "> ?p ?o}");
        }

    }

    private String replaceAllString(String sout, String exp, String string) {
        exp = exp.replaceAll("\\?", "\\\\?");
        exp = exp.replaceAll("\\*", "\\\\*");
        exp = exp.replaceAll("\\.", "\\\\.");
        return sout.replaceAll(exp, string);
    }
    public PiOnt getPiont() {
        return piont;
    }
}

class TwoVar {

    String v1;
    String v2;

    public TwoVar(String v1, String v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public String getV1() {
        return v1;
    }

    public String getV2() {
        return v2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass() && this == obj) {
            return true;
        }

        try {
            final TwoVar other = (TwoVar) obj;

            return (other.getV1().equals(v1) && other.getV2().equals(v2)) || (other.getV2().equals(v1) && other.getV1().equals(v2));

        } catch (Exception e) {
            return false;
        }
    }

}
