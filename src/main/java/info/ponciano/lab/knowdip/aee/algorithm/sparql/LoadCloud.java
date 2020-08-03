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
package info.ponciano.lab.knowdip.aee.algorithm.sparql;


import info.ponciano.lab.jpc.pointcloud.Pointcloud;
import info.ponciano.lab.knowdip.Knowdip;
import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.Algorithm;
import info.ponciano.lab.knowdip.aee.memory.Memory;
import java.util.Collections;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import java.io.IOException;
import java.util.UUID;


public class LoadCloud extends Algorithm {

    String hasSource;
    public static final String PATH = "hasSource";

    @Override
    protected Iterable<Node> process() throws KnowdipException {
         System.out.println("Point cloud loading: ");
        try {
            Pointcloud cloud;
            if (hasSource == null) {
                cloud = new Pointcloud();
            } else {
                cloud = new Pointcloud();
                cloud.loadASCII(hasSource);
                System.out.println("Point cloud loaded: " + cloud.size());
            }
            Node s = Knowdip.createURI(UUID.randomUUID().toString());
            Memory.get().alloc(s.getURI(), cloud);
            return Collections.singleton(s);
        } catch (IOException e) {
            throw new KnowdipException("The file  " + hasSource + " does not exist of cannot be open!");
        }
    }

}
