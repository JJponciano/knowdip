/*
 * Copyright (C) 2020 Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jean-jacques ponciano
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
