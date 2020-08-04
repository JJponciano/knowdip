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
package info.ponciano.lab.knowdip.aee.algorithm.sparql.getter;

import info.ponciano.lab.jpc.pointcloud.Pointcloud;
import info.ponciano.lab.jpc.pointcloud.components.APointCloud;
import info.ponciano.lab.knowdip.aee.KnowdipException;
import info.ponciano.lab.knowdip.aee.algorithm.sparql.Algorithm;
import info.ponciano.lab.knowdip.aee.memory.WritableResource;
import java.util.Collections;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 *
 * @author Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
 */
public class GetPatchDistanceX extends Algorithm {

    WritableResource hasInput;
    String patchID;

    @Override
    protected Iterable<Node> process() throws KnowdipException {
        Object data = hasInput.getData();
        if (!data.getClass().equals(Pointcloud.class)) {
            System.err.println("hasInput is not a point cloud in info.ponciano.lab.knowdip.aee.algorithm.mappc.GetPatchDistanceX.process()");
        } else {
            Pointcloud pc = (Pointcloud) data;
            String localName =patchID.substring(patchID.lastIndexOf('#')+1,patchID.length());
            APointCloud patch = pc.get(localName);
            double value = patch.getDx();
            
            return Collections.singleton(NodeFactory.createLiteralByValue(value,
                    XSDDatatype.XSDinteger));
        }
        return null;
    }
}
