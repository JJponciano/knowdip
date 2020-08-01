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


package info.ponciano.lab.knowdip.reasoner.automatic;

import info.ponciano.lab.knowdip.KD;
import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.pisemantic.PiDatatype;
import info.ponciano.lab.pisemantic.PiOnt;
import info.ponciano.lab.pisemantic.PiOntologyException;
import info.ponciano.lab.pisemantic.PiRestriction;
import info.ponciano.lab.pisemantic.PiRestrictions;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;

/**
 * This class is used to extract information from a semantic representation of
 * an object.
 *
 * @author Jean-Jacques Ponciano
 */
public class SemObject extends SemParsingObject {

    /**
     * Contains all property with the corresponding object type for every values
     * of the objects.
     */
    private PiRestrictions values;
    /**
     * Contains all property with the corresponding object type for every
     * characteristics of the object .
     */
    private PiRestrictions characteristics;

    public SemObject(OntClass ontClass, PiOnt piont) {
        super(ontClass, piont);
        this.init();
    }

    private void init() {
        try {
            this.values = this.filteringIsSub(KD.HAS_VALUE);
            this.characteristics = this.filteringIsSub(KD.HAS_CHARA);
        } catch (KnowdipException ex) {
            Logger.getLogger(SemObject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public PiRestrictions getValues() {
        return values;
    }

    public PiRestrictions getCharacteristics() {
        return characteristics;
    }

    @Override
    public String toString() {
        return "SemObject{" + "values=" + values + ", characteristics=" + characteristics + '}';
    }

    public String getClassif() throws PiOntologyException {
        int v = 0;
        //build the query
        String query = "CONSTRUCT { ?ind rdf:type <" + this.origin.getURI() + "> . ";
        String where = "";
        for (PiRestriction restriction : this.restrictions.toList()) {
            OntProperty property = restriction.getProperty();
            OntResource resource = (OntResource) restriction.getResource();
            //exepted hasValue
            if (restriction.isValue() || resource.isIndividual()) {
                where += "?ind <" + property.getURI() + "> <" + resource.getURI() + "> . ";
            } else {
                v++;
                // if it is a datatype restriction
                if (resource.isClass()
                        && resource.asClass().isHierarchyRoot()
                        && resource.getRDFType().getLocalName().equals("Datatype")) {
                    //create the datatype restriction
                    PiDatatype datatype = new PiDatatype(resource);

                    where += "?ind <" + property.getURI() + "> ?v" + v + " . ";
                    //get the translation in sparql
                    where += datatype.getSparqlFilter("?v" + v);
                } else {
                    where += "?ind <" + property.getURI() + "> ?v" + v + " . ";
                    where += "?v" + v + " rdf:type <" + resource.getURI() + "> . ";

                }
            }

        }
        if (where.isEmpty()) {
            return "";
        }
        query += "} WHERE { " + where + " }";
        return query;
    }

}
