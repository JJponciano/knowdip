/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.ponciano.lab.knowdip.reasoner;

import java.util.Objects;
import org.apache.jena.ontology.AllValuesFromRestriction;
import org.apache.jena.ontology.HasValueRestriction;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.ontology.SomeValuesFromRestriction;
import org.apache.jena.rdf.model.Resource;

/**
 *
 * @author jean-Jacques Ponciano
 */
public class PiRestriction {

    protected OntProperty pprt;
    protected Resource type;
    protected boolean isSome;
    protected boolean isOnly;
    protected boolean isValue;

    public PiRestriction(Restriction res) {
        this.isValue = false;
        this.isOnly = false;
        this.isSome = false;
        this.pprt = res.getOnProperty();
        //if it is a all values from restrictions
        if (res.isAllValuesFromRestriction()) {
            AllValuesFromRestriction onlyRes = res.asAllValuesFromRestriction();
            this.type = onlyRes.getAllValuesFrom();
            this.isOnly = true;

        } else if (res.isHasValueRestriction()) {
            HasValueRestriction hasV = res.asHasValueRestriction();
            this.type = hasV.getHasValue().asResource();
            this.isValue = true;

        } else if (res.isSomeValuesFromRestriction()) {
            SomeValuesFromRestriction someRes = res.asSomeValuesFromRestriction();
            this.type = someRes.getSomeValuesFrom().asResource();
            this.isSome = true;
        }
    }

    public OntProperty getProperty() {
        return pprt;
    }

    public Resource getResource() {
        return type;
    }

    @Override
    public String toString() {
        return pprt + " " + type + '}';
    }

    public boolean isSome() {
        return isSome;
    }

    public boolean isOnly() {
        return isOnly;
    }

    public boolean isValue() {
        return isValue;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.pprt);
        hash = 41 * hash + Objects.hashCode(this.type);
        hash = 41 * hash + (this.isSome ? 1 : 0);
        hash = 41 * hash + (this.isOnly ? 1 : 0);
        hash = 41 * hash + (this.isValue ? 1 : 0);
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
        final PiRestriction other = (PiRestriction) obj;
        if (this.isSome != other.isSome) {
            return false;
        }
        if (this.isOnly != other.isOnly) {
            return false;
        }
        if (this.isValue != other.isValue) {
            return false;
        }
        if (!Objects.equals(this.pprt, other.pprt)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        return true;
    }

}
