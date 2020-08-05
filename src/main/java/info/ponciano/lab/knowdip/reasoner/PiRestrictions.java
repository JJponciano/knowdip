/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package info.ponciano.lab.knowdip.reasoner;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jean-jacques
 */
public class PiRestrictions {

    protected List<PiRestriction> restrictions;

    public PiRestrictions() {
        this.restrictions = new ArrayList<>();
    }

    public PiRestrictions(PiRestrictions r) {
        this.restrictions = new ArrayList<>();
        this.addAll(r);

    }

    /**
     * Adds new restriction
     *
     * @param res restriction to be added
     */
    public void add(PiRestriction res) {
        this.restrictions.add(res);
    }

    /**
     * Test if the restriction is equals to another restriction already
     * contained.
     *
     * @param res restriction to be tested
     * @return true if the restriction is contained, false otherwise.
     */
    public boolean contains(PiRestriction res) {
        return (this.restrictions.contains(res));
    }

    /**
     * Test if the restriction has a property equals to another restriction's
     * property already contained.
     *
     * @param res restriction to be tested
     * @return true if the restriction's property is contained, false otherwise.
     */
    public boolean containsSameProperty(PiRestriction res) {
        return this.restrictions.stream().anyMatch((restriction) -> (restriction.getProperty().equals(res.getProperty())));
    }

    public List<PiRestriction> toList() {
        return restrictions;
    }

    /**
     * Add all restrictions
     *
     * @param res restrictions to be added.
     */
    public void addAll(PiRestrictions res) {
        res.toList().forEach((re) -> {
            this.add(re);
        });
    }

    public boolean isEmpty() {
        return this.restrictions.isEmpty();
    }

    public PiRestriction getFirst() {
        if (this.isEmpty()) {
            return null;
        } else {
            return this.restrictions.get(0);
        }
    }

    public int size() {
        return this.restrictions.size();
    }

    public PiRestriction get(int i) {
        if (i >= 0 && i < this.size()) {
            return this.restrictions.get(i);
        } else {
            return null;
        }
    }

    public void removeAt(int index) {
        if (index >= 0 && index < this.size()) {
            this.restrictions.remove(index);
        }
    }

}
