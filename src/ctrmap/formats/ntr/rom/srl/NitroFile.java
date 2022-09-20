/*
 * This file is part of jNdstool.
 *
 * jNdstool is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jNdstool. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2020-2021 JackHack96, Hello007
 */
package ctrmap.formats.ntr.rom.srl;

/**
 * This class represents a generic file in the Nitro file system
 */
class NitroFile implements Comparable<NitroFile> {
    public int id; // The file ID inside the ROM
    public int offset; // Absolute offset of the file
    public int size; // Size of the file
    public String name; // Name of the file
    public NitroDirectory parent; // Parent directory

    public NitroFile(String name, int id, int offset, int size, NitroDirectory parent) {
        this.name = name;
        this.id = id;
        this.offset = offset;
        this.size = size;
        this.parent = parent;
    }

    @Override
    public int compareTo(NitroFile nitroFile) {
        return name.compareToIgnoreCase(nitroFile.name);
    }

    @Override
    public String toString() {
        return "NitroFile{" +
                "name='" + name + '\'' +
                ", id=" + id +
                '}';
    }
}
