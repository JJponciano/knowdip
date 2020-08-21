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

import info.ponciano.lab.knowdip.Knowdip;
import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.Algorithm;
import info.ponciano.lab.knowdip.aee.memory.Memory;
import info.ponciano.lab.knowdip.reasoner.automatic.PiRegex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;

/**
 *
 * @author Jean-Jacques Ponciano
 */
public abstract class KReasoner {

    protected abstract Kee getKee();

    public void inferRoot() throws KnowdipException {
        this.getKee().construct("CONSTRUCT { ?x rdf:type ?sub } WHERE {"
                + "?x rdf:type ?t . ?t rdfs:subClassOf ?sub . "
                + "} ");
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
        List<String> vars = Knowdip.getSparqlVar(select);
        List<String> updateQuery = this.getInsert(select, vars, out, insert);
        if (updateQuery.isEmpty()) {
            return false;
        }
        this. getKee().update(updateQuery);
//        updateQuery.forEach((query) -> {
//            try {
//                //                 System.out.println(query);
//                this. getKee().update(query);
//            } catch (KnowdipException ex) {
//                Logger.getLogger(KReasoner.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        });
        return true;
    }


    protected List<String> getInsert(String selectQuery, List<String> vars, String selectOut, String maj) {
        List<String> updateQuery = new LinkedList<>();
        Iterator<KSolution> select = this. getKee().select(selectQuery);
        //select all the variables needed

        List<String> executeMemory = new LinkedList<>();
        //get every elements
        while (select.hasNext()) {
            KSolution next = select.next();
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
                Iterator<KSolution> selectOutrs = this. getKee().select(sout);
                //retrieve the result.
                while (selectOutrs.hasNext()) {
                    KSolution nextOut = selectOutrs.next();
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

    protected String getV(RDFNode get) {
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
        List<String> vars = Knowdip.getSparqlVar(temp);

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
    public void remove(String query) throws KnowdipException {
        PiRegex reg = new PiRegex("REMOVE\\s*(.*?)[\\s|{]", query);
        String var = reg.getGroup(1).get(0);
        query = query.replaceFirst("REMOVE|remove", "SELECT");

        Iterator<KSolution> select = this. getKee().select(query);
        List<String> uris = new ArrayList();
        while (select.hasNext()) {
            KSolution next = select.next();
            var get = next.get(var);
            uris.add(get.asResource().getURI());
        }
        for (String uri : uris) {
            this. getKee().update("DELETE WHERE {<" + uri + "> ?p ?o}");
        }

    }

    protected String replaceAllString(String sout, String exp, String string) {
        exp = exp.replaceAll("\\?", "\\\\?");
        exp = exp.replaceAll("\\*", "\\\\*");
        exp = exp.replaceAll("\\.", "\\\\.");
        return sout.replaceAll(exp, string);
    }

    public Memory getMemory() {
        return this. getKee().getMemory();
    }

    public void add(Class<? extends Algorithm> algo) throws KnowdipException {
        this. getKee().add(algo);
    }

    public void update(String query) throws KnowdipException {
        this. getKee().update(query);
    }

    public boolean construct(String queryString) throws KnowdipException {
        return this. getKee().construct(queryString);
    }

    public void close() {
        this. getKee().close();
    }

    public Iterator<KSolution> select(String query) {
        return this. getKee().select(query);
    }

    public String selectAsText(String query) {
        return this. getKee().selectAsText(query);
    }

    public OntModel getModel() {
        return this. getKee().getModel();
    }
}
