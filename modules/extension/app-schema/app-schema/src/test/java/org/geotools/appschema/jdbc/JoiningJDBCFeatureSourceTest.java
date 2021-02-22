/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2021, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package org.geotools.appschema.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import org.easymock.EasyMock;
import org.geotools.data.joining.JoiningQuery;
import org.geotools.data.postgis.PostGISDialect;
import org.geotools.data.store.ContentEntry;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.jdbc.JDBCFeatureSource;
import org.geotools.jdbc.PrimaryKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.feature.simple.SimpleFeatureType;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JDBCDataStore.class)
public class JoiningJDBCFeatureSourceTest {

    @Test
    public void testMultipleIds() throws Exception {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("test");
        SimpleFeatureType origType = builder.buildFeatureType();

        JoiningQuery query = new JoiningQuery();
        JoiningQuery.QueryJoin join = new JoiningQuery.QueryJoin();
        join.getIds().add("one");
        join.getIds().add("two");
        query.setQueryJoins(Collections.singletonList(join));

        JDBCDataStore mockStore = PowerMock.createNiceMock(JDBCDataStore.class);
        ContentEntry mockEntry = PowerMock.createNiceMock(ContentEntry.class);
        PowerMock.expectPrivate(
                        mockStore,
                        JDBCDataStore.class.getMethod("getPrimaryKey", SimpleFeatureType.class),
                        origType)
                .andReturn(new PrimaryKey(null, Collections.emptyList()))
                .anyTimes();
        EasyMock.expect(mockStore.getSQLDialect())
                .andReturn(new PostGISDialect(mockStore))
                .anyTimes();
        EasyMock.replay(mockStore);
        EasyMock.expect(mockEntry.getDataStore()).andReturn(mockStore).anyTimes();
        EasyMock.replay(mockEntry);
        JoiningJDBCFeatureSource source =
                new JoiningJDBCFeatureSource(new JDBCFeatureSource(mockEntry, null));
        SimpleFeatureType type = source.getFeatureType(origType, query);
        assertNotNull(type);
        assertEquals("FOREIGN_ID_0_0", type.getDescriptor(0).getName().getLocalPart());
        assertEquals("FOREIGN_ID_0_1", type.getDescriptor(1).getName().getLocalPart());
    }
}
