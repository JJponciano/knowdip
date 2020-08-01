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

import java.util.Objects;
import org.apache.jena.ontology.OntProperty;

/**
 *
 * @author Jean-Jacques
 */
public class PropVar {

    protected OntProperty property;
    protected String variable;

    public PropVar(OntProperty property, String variable) {
        this.property = property;
        this.variable = variable;
    }

    public OntProperty getOntProperty() {
        return property;
    }

    public void setOntProperty(OntProperty property) {
        this.property = property;
    }

    public String getVariable() {
        return variable;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.property);
        hash = 31 * hash + Objects.hashCode(this.variable);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PropVar other = (PropVar) obj;
        if (!Objects.equals(this.property, other.property)) {
            return false;
        }
        if (!Objects.equals(this.variable, other.variable)) {
            return false;
        }
        return true;
    }

    /**
     * Test if the object has a property.
     *
     * @return true if the property is not null, false otherwise.
     */
    public boolean hasOntProperty() {
        return this.property != null;
    }

}
