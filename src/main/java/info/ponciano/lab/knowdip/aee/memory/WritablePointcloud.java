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
import java.io.IOException;

public class WritablePointcloud implements WritableResource<Pointcloud> {

    private final Pointcloud cloud;
    private static final String EXT = "xyz";

    WritablePointcloud(Pointcloud cloud) {
        this.cloud = cloud;
    }

    WritablePointcloud() {
        this.cloud = new Pointcloud();
    }

    @Override
    public void write(String path) throws IOException {
        cloud.saveASCII(path);
    }

    @Override
    public void read(String path) throws IOException {
        cloud.loadASCII(path);
    }

    @Override
    public String getExt() {
        return this.EXT;
    }

    @Override
    public Pointcloud getData() {
        return this.cloud;
    }

    @Override
    public boolean hasRightExt(String path) {
        return path.endsWith(EXT);
    }

}
