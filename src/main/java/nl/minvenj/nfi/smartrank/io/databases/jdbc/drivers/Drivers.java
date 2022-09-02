/*
 * Copyright (C) 2021 Netherlands Forensic Institute
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package nl.minvenj.nfi.smartrank.io.databases.jdbc.drivers;

public enum Drivers {
    H2("H2"), POSTGRESQL("PostgreSQL"), SQLSERVER("SQLServer"), SYBASE("Sybase");
    
    private String _name;
    
    private Drivers(String name) {
        _name = name;
    }
    
    public static Drivers findByName(String name) {
         for(Drivers driver : Drivers.values()) {
             if(driver.getName().equalsIgnoreCase(name))
                 return driver;
         }
         throw new IllegalArgumentException("No driver with name '"+name+"' exists!");
    }

    public String getName() {
        return _name;
    }
}
