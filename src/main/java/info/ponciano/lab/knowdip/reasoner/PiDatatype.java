/*
 * Copyright (C) 2020 Dr Jean-Jacques Ponciano (Contact: jean-jacques@ponciano.info)
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

import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 *
 * @author jean-jacques Ponciano
 */
public class PiDatatype {

    private Property first;
    private Literal firstLit;
    private final static String MIN_INCLUSIVE = "http://www.w3.org/2001/XMLSchema#minInclusive";
    private final static String MAX_INCLUSIVE = "http://www.w3.org/2001/XMLSchema#maxInclusive";
    private final static String MAX = "http://www.w3.org/2001/XMLSchema#maxExclusive";
    private final static String MIN = "http://www.w3.org/2001/XMLSchema#minExclusive";

    /**
     * Creates the datatype corresponding to the resource.
     *
     * @param resource resource to be converted
     */
    public PiDatatype(OntResource resource) {
        StmtIterator listProperties = resource.listProperties();
        while (listProperties.hasNext()) {
            Statement next = listProperties.next();
            Property predicate = next.getPredicate();
            if (predicate.getLocalName().equals("withRestrictions")) {
                //get the restriction
                StmtIterator withRestrictions = next.getResource().listProperties();
                while (withRestrictions.hasNext()) {
                    Statement restriction = withRestrictions.next();
                    //if it is the first restriction
                    if (restriction.getPredicate().getLocalName().equals("first")) {
                        //get all property and value restriction
                        StmtIterator lps = restriction.getResource().listProperties();
                        while (lps.hasNext()) {
                            Statement next2 = lps.next();
                            this.first = next2.getPredicate();
                            this.firstLit = next2.getLiteral();
                        }
                    }
                }
            }
        }
    }

    public Property getFirst() {
        return first;
    }

    public Literal getFirstLit() {
        return firstLit;
    }

    /**
     * Get the sparql filter query part corresponding to the datatype
     * restriction
     * <p>
     * Example of parsing:
     * <pre>
     * PiDatatype{ first=http://www.w3.org/2001/XMLSchema#minInclusive, firstLit=5.0^^http://www.w3.org/2001/XMLSchema#double}
     * => FILTER( ?v  &gt= 5.0 )
     * </pre>
     * </p>
     *
     * @param varname variable name used.
     * @return the sparql query corresponding to the datatype.
     */
    public String getSparqlFilter(String varname) throws PiOntologyException {
        String sparql = "FILTER( " + varname + " ";
        //if the first restriction is minInclusive
        if (this.first.getURI().equals(this.MIN_INCLUSIVE)) {
            sparql += " >= ";
        } else if (this.first.getURI().equals(this.MIN)) {
            sparql += " > ";
        } else if (this.first.getURI().equals(this.MAX)) {
            sparql += " < ";
        } else if (this.first.getURI().equals(this.MAX_INCLUSIVE)) {
            sparql += " <= ";
        } else {
            String uri = this.first.getURI();
            System.err.println(uri);
            throw new PiOntologyException(uri + " unknow datatype");
        }

        sparql += this.firstLit.getLexicalForm();
        sparql += " ) ";
        return sparql;
    }

    @Override
    public String toString() {
        return "PiDatatype{ first=" + first + ", firstLit=" + firstLit + '}';
    }

}
