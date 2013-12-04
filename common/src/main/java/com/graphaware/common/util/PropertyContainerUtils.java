/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.common.util;

import com.graphaware.common.change.Change;
import com.graphaware.common.strategy.IncludeAll;
import com.graphaware.common.strategy.InclusionStrategy;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.graphaware.common.util.ArrayUtils.isPrimitiveOrStringArray;
import static com.graphaware.common.util.ArrayUtils.primitiveOrStringArrayToString;

/**
 * Utility methods for dealing with {@link org.neo4j.graphdb.PropertyContainer}s.
 */
public final class PropertyContainerUtils {

    /**
     * Convert a collection of {@link org.neo4j.graphdb.PropertyContainer}s to a map of {@link org.neo4j.graphdb.PropertyContainer}s keyed by their ID.
     *
     * @param propertyContainers to convert.
     * @param <T>                type of the {@link org.neo4j.graphdb.PropertyContainer}.
     * @return map keyed by {@link org.neo4j.graphdb.PropertyContainer} ID with the actual {@link org.neo4j.graphdb.PropertyContainer}s as values.
     */
    public static <T extends PropertyContainer> Map<Long, T> propertyContainersToMap(Collection<T> propertyContainers) {
        Map<Long, T> result = new HashMap<>();

        for (T propertyContainer : propertyContainers) {
            result.put(id(propertyContainer), propertyContainer);
        }

        return result;
    }

    /**
     * Convert a collection of {@link Change}s of {@link org.neo4j.graphdb.PropertyContainer} to a map of {@link Change}s keyed by the
     * {@link org.neo4j.graphdb.PropertyContainer} ID.
     *
     * @param changes to convert.
     * @param <T>     type of the {@link org.neo4j.graphdb.PropertyContainer}.
     * @return map keyed by {@link org.neo4j.graphdb.PropertyContainer} ID with the actual {@link Change}s as values.
     * @throws IllegalArgumentException in case the two {@link org.neo4j.graphdb.PropertyContainer}s contained in a {@link Change} do not
     *                                  have the same IDs.
     */
    public static <T extends PropertyContainer> Map<Long, Change<T>> changesToMap(Collection<Change<T>> changes) {
        Map<Long, Change<T>> result = new HashMap<>();

        for (Change<T> change : changes) {
            long id = id(change.getPrevious());
            if (id != id(change.getCurrent())) {
                throw new IllegalArgumentException("IDs of the Property Containers in Change do not match!");
            }
            result.put(id, change);
        }

        return result;
    }

    /**
     * Get ID from a {@link org.neo4j.graphdb.PropertyContainer}.
     *
     * @param propertyContainer to get ID from. Must be a {@link org.neo4j.graphdb.Node} or {@link org.neo4j.graphdb.Relationship}.
     * @return ID
     * @throws IllegalStateException in case the propertyContainer is not a {@link org.neo4j.graphdb.Node} or a {@link org.neo4j.graphdb.Relationship}.
     */
    public static long id(PropertyContainer propertyContainer) {
        if (Node.class.isAssignableFrom(propertyContainer.getClass())) {
            return ((Node) propertyContainer).getId();
        }

        if (Relationship.class.isAssignableFrom(propertyContainer.getClass())) {
            return ((Relationship) propertyContainer).getId();
        }

        throw new IllegalStateException("Unknown Property Container: " + propertyContainer.getClass().getName());
    }

    /**
     * Make sure a key is not null or empty.
     *
     * @param key the key to check.
     * @return the same key.
     * @throws IllegalArgumentException if null or empty String.
     */
    public static String cleanKey(String key) {
        if (key == null || "".equals(key.trim())) {
            throw new IllegalArgumentException("Key must not be null or empty!");
        }
        return key;
    }

    /**
     * Convert a property value to String. If the value is <code>null</code>, then it will be converted to an empty String.
     *
     * @param value to convert.
     * @return property value as String.
     */
    public static String valueToString(Object value) {
        if (value == null) {
            return "";
        }

        if (isPrimitiveOrStringArray(value)) {
            return primitiveOrStringArrayToString(value);
        }
        return String.valueOf(value);
    }

    /**
     * Convert all properties from a {@link org.neo4j.graphdb.PropertyContainer} to a {@link java.util.Map} of {@link String}s, where the key is the
     * property key and value is the property value converted to {@link String}. Keys must not be <code>null</code>
     * (not allowed by Neo4j anyway) or empty (not allowed by GraphAware). <code>Null</code> values will be converted
     * to empty {@link String}s.
     *
     * @param propertyContainer to convert properties from.
     * @return converted properties.
     */
    public static Map<String, String> propertiesToStringMap(PropertyContainer propertyContainer) {
        return propertiesToStringMap(propertyContainer, new IncludeAll<String>());
    }

    /**
     * Convert selected properties from a {@link org.neo4j.graphdb.PropertyContainer} to a {@link java.util.Map} of {@link String}s, where the key is the
     * property key and value is the property value converted to {@link String}. Keys must not be <code>null</code>
     * (not allowed by Neo4j anyway) or empty (not allowed by GraphAware). <code>Null</code> values will be converted
     * to empty {@link String}s.
     *
     * @param propertyContainer         to convert properties from.
     * @param propertyInclusionStrategy strategy to select which properties to include. Decides based on the property key.
     * @return converted properties.
     */
    public static Map<String, String> propertiesToStringMap(PropertyContainer propertyContainer, InclusionStrategy<String> propertyInclusionStrategy) {
        Map<String, String> result = new HashMap<>();
        for (String key : propertyContainer.getPropertyKeys()) {
            if (propertyInclusionStrategy.include(key)) {
                result.put(cleanKey(key), valueToString(propertyContainer.getProperty(key)));
            }
        }
        return result;
    }

    /**
     * Convert all properties from a {@link org.neo4j.graphdb.PropertyContainer} to a {@link java.util.Map}, where the key is the
     * property key and value is the property value. Keys must not be <code>null</code>
     * (not allowed by Neo4j anyway) or empty (not allowed by GraphAware). <code>Null</code> values are fine.
     *
     * @param propertyContainer to convert properties from.
     * @return converted properties.
     */
    public static Map<String, Object> propertiesToObjectMap(PropertyContainer propertyContainer) {
        return propertiesToObjectMap(propertyContainer, new IncludeAll<String>());
    }

    /**
     * Convert selected properties from a {@link org.neo4j.graphdb.PropertyContainer} to a {@link java.util.Map}, where the key is the
     * property key and value is the property value. Keys must not be <code>null</code>
     * (not allowed by Neo4j anyway) or empty (not allowed by GraphAware). <code>Null</code> values are fine.
     *
     * @param propertyContainer         to convert properties from.
     * @param propertyInclusionStrategy strategy to select which properties to include. Decides based on the property key.
     * @return converted properties.
     */
    public static Map<String, Object> propertiesToObjectMap(PropertyContainer propertyContainer, InclusionStrategy<String> propertyInclusionStrategy) {
        Map<String, Object> result = new HashMap<>();
        for (String key : propertyContainer.getPropertyKeys()) {
            if (propertyInclusionStrategy.include(key)) {
                result.put(cleanKey(key), propertyContainer.getProperty(key));
            }
        }
        return result;
    }

    /**
     * Convert properties from a {@link java.util.Map} to a {@link java.util.Map} of "clean properties", where the key is the
     * property key and value is the property value converted to {@link String}. Keys must not be <code>null</code>
     * (not allowed by Neo4j anyway) or empty (not allowed by GraphAware). <code>Null</code> values will be converted
     * to empty {@link String}s.
     *
     * @param properties to cleanup
     * @return cleaned properties.
     */
    public static Map<String, String> cleanStringProperties(Map<String, ?> properties) {
        Map<String, String> result = new HashMap<>();
        for (String key : properties.keySet()) {
            result.put(cleanKey(key), valueToString(properties.get(key)));
        }
        return result;
    }

    /**
     * Convert properties from a {@link java.util.Map} to a {@link java.util.Map} of "clean properties", where the key is the
     * property key and value is the property value. Keys must not be <code>null</code>
     * (not allowed by Neo4j anyway) or empty (not allowed by GraphAware).
     *
     * @param properties to cleanup
     * @return cleaned properties.
     */
    public static Map<String, Object> cleanObjectProperties(Map<String, ?> properties) {
        Map<String, Object> result = new HashMap<>();
        for (String key : properties.keySet()) {
            result.put(cleanKey(key), properties.get(key));
        }
        return result;
    }

    /**
     * Delete a node, but delete all its relationships first.
     * This method assumes a transaction is in progress.
     *
     * @param toDelete node to delete along with its relationships.
     * @return number of deleted relationships.
     */
    public static int deleteNodeAndRelationships(Node toDelete) {
        int result = 0;
        for (Relationship relationship : toDelete.getRelationships()) {
            relationship.delete();
            result++;
        }
        toDelete.delete();
        return result;
    }

    private PropertyContainerUtils() {
    }
}