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

import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.reasoner.PiOnt;
import info.ponciano.lab.knowdip.reasoner.PiRestrictions;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;

/**
 *
 * @author Jean-Jacques Ponciano
 */
public abstract class SemParsingObject {

    protected final OntClass origin;
    protected final PiOnt piont;
    protected PiRestrictions restrictions;

    public SemParsingObject(OntClass origin, PiOnt piont) {
        this.origin = origin;
        this.piont = piont;
        this.restrictions = this.piont.getRestrictions(this.origin);
    }

    public OntClass getOrigin() {
        return origin;
    }

    /**
     * Filter all restrictions on property corresponding to the property specify
     * or a sub-property of it, without remove restrictions of the list.
     *
     * @param uriPrpt property used for the filtering
     * @return list of restrictions filtered.
     */
    protected PiRestrictions filteringIsSub(String uriPrpt) throws KnowdipException {
        OntProperty prpt = this.piont.getOntProperty(uriPrpt);
        PiRestrictions values = new PiRestrictions();
        if (prpt == null) {
            throw new KnowdipException("The property  " + uriPrpt + " does not exist in the ontology!");
        } else {
            this.restrictions.toList().forEach((restriction) -> {
                OntProperty pprt = restriction.getProperty();
                //test if the property is an hasValue property or a sub property of hasValue
                if (prpt.equals(pprt) || prpt.hasSubProperty(pprt, false)) {
                    values.add(restriction);
                }
            });
        }
        return values;
    }
}
