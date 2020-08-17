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
package info.ponciano.lab.knowdip.aee.memory;

import info.ponciano.lab.jpc.pointcloud.Pointcloud;
import info.ponciano.lab.jpc.pointcloud.components.APointCloud;
import info.ponciano.lab.knowdip.Knowdip;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.BadAttributeValueExpException;
import org.apache.commons.io.FileUtils;

/**
 * Generates unique identifiers and stores instances of WritableResource.
 *
 * @see info.ponciano.lab.knowdip.aee.WritableResource
 * @author Dr Jean-Jacques Ponciano <jean-jacques@ponciano.info>
 */
public class Memory {

    private Map<String, WritableResource> data;

    public Memory() {
        super();
        this.data = new HashMap<>();
    }

    /**
     * Allocates a point cloud with the given URI
     *
     * @param cloud point cloud to be allocated
     * @param uri URI of the corresponding individual inside the ontology
     */
    public void alloc(String uri, Pointcloud cloud) {
        this.data.put(uri, new WritablePointcloud(cloud));
    }
//

    /**
     * Allocates a point cloud and creates an URI
     *
     * @param cloud point cloud to be allocated
     * @return URI of the corresponding individual inside the ontology
     */
    public String alloc(Pointcloud cloud) {
        String uri = Knowdip.createURI(cloud.getClass().getSimpleName()
                + "_" + UUID.randomUUID().toString()).getURI();
        this.alloc(uri, cloud);
        return uri;
    }

    public void replace(String addr, Object o) {
        if (!o.getClass().equals(Pointcloud.class)) {
            throw new InternalError("Alloc not implemented for " + o.getClass());
        } else {
            Pointcloud cloud = (Pointcloud) o;
            this.data.replace(addr, new WritablePointcloud(cloud));
        }
    }

    public boolean free(String addr) {
        if (this.data.containsKey(addr)) {
            this.data.remove(addr);
            return true;
        } else {
            return false;
        }
    }

    public Object access(String addr) {
        WritableResource obj = this.data.get(addr);

        //if the object get is null
        if (obj == null) {
            //perhaps it is a patch
            //extract the patch name from the addr.
            String name = addr.substring(addr.lastIndexOf('#') + 1, addr.length());
            //get all point cloud
            List<String> clouds = Knowdip.get().listPointClouds();
            //Test for each cloud if it contains the name as a patch
            for (String c : clouds) {
                //test if the data is a pointcloud
                Object get1 = this.data.get(c).getData();
                if (!get1.getClass().equals(Pointcloud.class)) {
                    throw new InternalError("The method  Knowdip.get().listPointClouds() returns an element that isn't a point cloud.");
                }
                Pointcloud pc = (Pointcloud) get1;
                APointCloud patch = pc.get(name);
                if (patch != null) {
                    return patch;
                }
            }
            return null;
        }else return obj.getData();
    }

    /**
     * Writes all point cloud in a directory. Warning if the directory exists it
     * will be cleaned.
     *
     * @param path path of the directory
     * @throws IOException if something wrong.
     */
    public void write(String path) throws IOException {
        //Clean the directory if it exists
        File dir = new File(path);
        if (dir.exists()) {
            if (dir.isDirectory()) {
                FileUtils.cleanDirectory(dir);
            }
            FileUtils.forceDelete(dir);
        }
        dir.mkdir();
        if (!path.endsWith("/") || !path.endsWith("\\")) {
            path += "/";
            final String filename = path;
            //save each element
            this.data.forEach((var k, var v) -> {
                try {
                    var n = k.substring(k.lastIndexOf('#') + 1, k.length());
                    v.write(filename + n + "." + v.getExt());
                } catch (IOException ex) {
                    Logger.getLogger(Memory.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    public void read(String path) throws IOException {
        this.data.clear();
        List<String> ls = this.ls(path);
        ls.forEach(k -> {
            WritablePointcloud writablePointcloud = new WritablePointcloud();
            //read point cloud
            if (writablePointcloud.hasRightExt(path)) {
                try {
                    writablePointcloud.read(path);
                    String[] split = k.split("\\.");
                    if (split == null || split.length == 0) {
                        throw new InternalError("the file " + k + " cannot be load in the memory");
                    }
                    this.data.put(split[0], writablePointcloud);
                } catch (IOException ex) {
                    Logger.getLogger(Memory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }

    public boolean contains(String uri) {
        return this.data.containsKey(uri);
    }

    private List<String> ls(final String directory) throws FileNotFoundException, IOException {

        final List<String> result = new ArrayList<>();
        final String[] listFile = new File(directory).list();
        for (final String listFile1 : listFile) {
            final File file = new File(directory + "/" + listFile1);
            if (file.isDirectory()) {
                result.addAll(ls(file.getPath()));
            } else {
                result.add(file.getName());
            }
        }
        return result;
    }

}
