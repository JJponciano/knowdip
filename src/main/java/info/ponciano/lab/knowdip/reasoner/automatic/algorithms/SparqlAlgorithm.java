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


package info.ponciano.lab.knowdip.reasoner.automatic.algorithms;

import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pisemantic.PiOntologyException;
import info.ponciano.lab.pitools.utility.PiRegex;
import java.util.LinkedList;
import java.util.List;
import org.apache.jena.ontology.OntClass;

/**
 *
 * @author Jean-Jacques
 */
public class SparqlAlgorithm extends SemanticAlgorithm {

    protected String selectQuery;
    private List<String> varCommon;
    private String execution;
    private String insert;

    public SparqlAlgorithm(OntClass origin, PiOnt piont) throws KnowdipException, PiOntologyException {
        super(origin, piont);
        this.selectQuery = "SELECT ";
        this.init();
    }

    public String getSelect() throws KnowdipException {
        return selectQuery;
    }

    public List<String> getVarSelected() {
        return varCommon;
    }

    protected List<String> extractVar(String where) {

        List<String> group = new PiRegex("\\?\\w+", where).getGroup(false);
        return group;
    }

    public List<String> getCommonElements(List<String> varWhere, List<String> varConstruct) {
        List<String> vars = new LinkedList<>();
        varWhere.stream().filter((v) -> (varConstruct.contains(v) && !vars.contains(v))).forEachOrdered((v) -> {
            vars.add(v);
        });
        return vars;
    }

    private void init() {
            //get the sparql query to integrate the output of the algorithm.
            this.insert = "INSERT DATA {" + this.getSparqlOutput() + "}";
            // get queries to retrieve object value and execute the algorithm
            String[] sparqlAlgoExecution = sparqlAlgoExecution();
            //build the where content of the sparql query
            String where = " WHERE{\n" + sparqlSelectInputs() + sparqlSelectParameters() + sparqlAlgoExecution[0] + " }";

            this.execution = "SELECT ?out WHERE{ " + sparqlAlgoExecution[1] + " }";
            //build the first select

            //get list of variable inside the where
            List<String> varWhere = this.extractVar(where);
            List<String> varInsert = this.extractVar(insert);
            List<String> varExecution = this.extractVar(execution);
            //get only common elements
            this.varCommon = this.mergeWithoutDouble(varExecution, this.getCommonElements(varWhere, varInsert));
            this.varCommon.remove("?out");
            //add the variable to be extracted
            varCommon.forEach((v) -> {
                selectQuery += " " + v;
            });
            //build the string query
            selectQuery += where;

    }

    public String getExec() {
        return this.execution;
    }

    public String getInsert() {
        return insert;
    }

    private List<String> mergeWithoutDouble(List<String> varExecution, List<String> commonElements) {
        List<String> vars = new LinkedList<>();
        varExecution.stream().filter((v) -> (!vars.contains(v))).forEachOrdered((v) -> {
            vars.add(v);
        });
        commonElements.stream().filter((v) -> (!vars.contains(v))).forEachOrdered((v) -> {
            vars.add(v);
        });
        return vars;
    }

    

}
