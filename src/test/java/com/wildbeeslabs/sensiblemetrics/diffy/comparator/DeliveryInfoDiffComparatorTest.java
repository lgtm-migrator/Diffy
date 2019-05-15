/*
 * The MIT License
 *
 * Copyright 2019 WildBees Labs, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.wildbeeslabs.sensiblemetrics.diffy.comparator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wildbeeslabs.sensiblemetrics.diffy.AbstractDeliveryInfoDiffTest;
import com.wildbeeslabs.sensiblemetrics.diffy.comparator.iface.DiffComparator;
import com.wildbeeslabs.sensiblemetrics.diffy.comparator.impl.DefaultDiffComparator;
import com.wildbeeslabs.sensiblemetrics.diffy.entry.impl.DefaultDiffEntry;
import com.wildbeeslabs.sensiblemetrics.diffy.examples.model.AddressInfo;
import com.wildbeeslabs.sensiblemetrics.diffy.examples.model.DeliveryInfo;
import com.wildbeeslabs.sensiblemetrics.diffy.factory.DefaultDiffComparatorFactory;
import com.wildbeeslabs.sensiblemetrics.diffy.utils.ComparatorUtils;
import com.wildbeeslabs.sensiblemetrics.diffy.utils.DateUtils;
import com.wildbeeslabs.sensiblemetrics.diffy.utils.ReflectionUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsEqual;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.wildbeeslabs.sensiblemetrics.diffy.utils.DateUtils.toDate;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Delivery info difference comparator unit test
 *
 * @author Alexander Rogalskiy
 * @version 1.1
 * @since 1.0
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeliveryInfoDiffComparatorTest extends AbstractDeliveryInfoDiffTest {

    /**
     * Default date time format pattern
     */
    private final String DEFAULT_DATETIME_FORMAT = "dd/MM/yyyy hh:mm:ss";

    /**
     * Default {@link DeliveryInfo} models
     */
    private DeliveryInfo deliveryInfoFirst;
    private DeliveryInfo deliveryInfoLast;

    @Before
    public void setUp() {
        this.deliveryInfoFirst = getDeliveryInfoMock().val();
        this.deliveryInfoLast = getDeliveryInfoMock().val();
    }

    @Test
    @DisplayName("Test comparing different delivery info entities by default comparator")
    public void test_differentEntities_by_defaultComparator() {
        // given
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);
        getDeliveryInfoFirst().setStatus(DeliveryInfo.DeliveryStatus.DELIVERED);
        getDeliveryInfoLast().setStatus(DeliveryInfo.DeliveryStatus.PENDING);

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), is(greaterThanOrEqualTo(0)));
        assertThat(valueChangeList.size(), Matchers.is(lessThanOrEqualTo(ReflectionUtils.getAllFields(DeliveryInfo.class).length)));

        assertEquals("createdAt", valueChangeList.get(0).getPropertyName());
        assertEquals(getDeliveryInfoFirst().getCreatedAt(), valueChangeList.get(0).getFirst());
        assertEquals(getDeliveryInfoLast().getCreatedAt(), valueChangeList.get(0).getLast());
        assertNotEquals(getDeliveryInfoFirst().getCreatedAt(), getDeliveryInfoLast().getCreatedAt());

        assertEquals("status", valueChangeList.get(valueChangeList.size() - 1).getPropertyName());
        assertEquals(getDeliveryInfoFirst().getStatus(), valueChangeList.get(valueChangeList.size() - 1).getFirst());
        assertEquals(getDeliveryInfoLast().getStatus(), valueChangeList.get(valueChangeList.size() - 1).getLast());
        assertNotEquals(getDeliveryInfoFirst().getStatus(), getDeliveryInfoLast().getStatus());
    }

    @Test
    @DisplayName("Test comparing equal delivery info entities by default comparator")
    public void test_equalEntities_by_defaultComparator() {
        // given
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoFirst());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList.size(), IsEqual.equalTo(0));
    }

    @Test
    @DisplayName("Test comparing delivery info entities by excluded properties and custom comparator")
    public void test_entitiesWithExcludedProperties_by_defaultComparator() {
        // given
        final List<String> excludedProperties = Arrays.asList("id", "createdAt", "updatedAt");

        // when
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class, DEFAULT_DELIVERY_INFO_COMPARATOR, excludedProperties);
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), is(greaterThanOrEqualTo(0)));
        assertThat(valueChangeList.size(), lessThanOrEqualTo(ReflectionUtils.getAllFields(DeliveryInfo.class).length - Sets.newHashSet(excludedProperties).size()));

        DefaultDiffEntry entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("createdAt", getDeliveryInfoFirst().getCreatedAt(), getDeliveryInfoLast().getCreatedAt());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("type", getDeliveryInfoFirst().getType(), getDeliveryInfoLast().getType());
        assertTrue(valueChangeList.contains(entry));

    }

    @Test
    @DisplayName("Test comparing delivery info entities by included properties, empty excluded properties and custom comparator")
    public void test_entitiesWithIncludedProperties_by_defaultComparator() {
        // given
        final List<String> includedProperties = Arrays.asList("id", "type", "description");
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class, DEFAULT_DELIVERY_INFO_COMPARATOR, includedProperties, Collections.emptyList());

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), is(greaterThanOrEqualTo(0)));
        assertThat(valueChangeList.size(), is(lessThanOrEqualTo(includedProperties.size())));

        DefaultDiffEntry entry = DefaultDiffEntry.of("createdAt", getDeliveryInfoFirst().getCreatedAt(), getDeliveryInfoLast().getCreatedAt());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("updatedAt", getDeliveryInfoFirst().getUpdatedAt(), getDeliveryInfoLast().getUpdatedAt());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertTrue(valueChangeList.contains(entry));
    }

    @Test
    @DisplayName("Test comparing delivery info entities by excluded properties and custom comparator")
    public void test_entitiesWithExcludedProperties_by_customComparator() {
        // given
        final List<String> excludedProperties = Arrays.asList("id", "type");
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class, DEFAULT_DELIVERY_INFO_COMPARATOR, excludedProperties);

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), is(greaterThanOrEqualTo(0)));
        assertThat(valueChangeList.size(), Matchers.is(lessThanOrEqualTo(ReflectionUtils.getAllFields(DeliveryInfo.class).length - Sets.newHashSet(excludedProperties).size())));

        DefaultDiffEntry entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("description", getDeliveryInfoFirst().getDescription(), getDeliveryInfoLast().getDescription());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getDescription(), getDeliveryInfoLast().getDescription());
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included properties, empty excluded properties and custom comparator")
    public void test_entitiesWithIncludedProperties_by_customComparator() {
        // given
        final List<String> includedProperties = Arrays.asList("id", "createdAt");
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class, DEFAULT_DELIVERY_INFO_COMPARATOR, includedProperties, Collections.emptyList());

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), is(greaterThanOrEqualTo(0)));
        assertThat(valueChangeList.size(), is(lessThanOrEqualTo(includedProperties.size())));

        DefaultDiffEntry entry = DefaultDiffEntry.of("updatedAt", getDeliveryInfoFirst().getUpdatedAt(), getDeliveryInfoLast().getUpdatedAt());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("createdAt", getDeliveryInfoFirst().getCreatedAt(), getDeliveryInfoLast().getCreatedAt());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getCreatedAt(), getDeliveryInfoLast().getCreatedAt());
    }

    @Test
    @DisplayName("Test comparing delivery info entities by excluded non-existing properties and default comparator")
    public void test_entitiesWithNonExistingProperty_by_defaultComparator() {
        // given
        final List<String> excludedProperties = Arrays.asList("id", "country", "createdAt", "updatedAt");
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class, excludedProperties);

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), Matchers.is(lessThanOrEqualTo(ReflectionUtils.getAllFields(DeliveryInfo.class).length - Sets.newHashSet(excludedProperties).size())));

        DefaultDiffEntry entry = DefaultDiffEntry.of("type", getDeliveryInfoFirst().getType(), getDeliveryInfoLast().getType());
        assertTrue(valueChangeList.contains(entry));

        assertTrue(valueChangeList.stream().noneMatch(value -> "_sums".equalsIgnoreCase(value.getPropertyName())));

        entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertFalse(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included / excluded intersected properties and default comparator")
    public void test_entitiesWithIntersectedProperties_by_defaultComparator() {
        // given
        final List<String> includedProperties = Arrays.asList("id", "createdAt", "updatedAt", "description");
        final List<String> excludedProperties = Arrays.asList("createdAt", "updatedAt", "type");
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class, includedProperties, excludedProperties);

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertEquals(CollectionUtils.subtract(includedProperties, excludedProperties).size(), valueChangeList.size());

        DefaultDiffEntry entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());

        entry = DefaultDiffEntry.of("createdAt", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("type", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("description", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getDescription(), getDeliveryInfoLast().getDescription());
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included / excluded non-intersected properties and default comparator")
    public void test_entitiesWithNonIntersectedProperties_by_defaultComparator() {
        // given
        final List<String> includedProperties = Arrays.asList("id", "type", "balance");
        final List<String> excludedProperties = Arrays.asList("description", "createdAt", "updatedAt");
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class, includedProperties, excludedProperties);

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertEquals(includedProperties.size(), valueChangeList.size());

        DefaultDiffEntry entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());

        entry = DefaultDiffEntry.of("description", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("createdAt", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertFalse(valueChangeList.contains(entry));
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included duplicate properties and default comparator")
    public void test_entitiesWithIncludedDuplicateProperty_by_defaultComparator() {
        // given
        final List<String> includedProperties = Arrays.asList("id", "type", "createdAt", "id", "type");
        final DefaultDiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);
        diffComparator.includeProperties(includedProperties);

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), IsEqual.equalTo(3));

        DefaultDiffEntry entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());

        entry = DefaultDiffEntry.of("description", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertNotEquals(getDeliveryInfoFirst().getDescription(), getDeliveryInfoLast().getDescription());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("updatedAt", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertFalse(valueChangeList.contains(entry));
    }

    @Test
    @DisplayName("Test comparing delivery info entities by excluded duplicate properties and default comparator")
    public void test_entitiesWithExcludedDuplicateProperty_by_defaultComparator() {
        // given
        final List<String> excludedProperties = Arrays.asList("id", "type", "description", "type", "type", "id");
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class, excludedProperties);

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), Matchers.is(lessThanOrEqualTo(ReflectionUtils.getAllFields(DeliveryInfo.class).length - Sets.newHashSet(excludedProperties).size())));

        DefaultDiffEntry entry = DefaultDiffEntry.of("createdAt", getDeliveryInfoFirst().getCreatedAt(), getDeliveryInfoLast().getCreatedAt());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getCreatedAt(), getDeliveryInfoLast().getCreatedAt());

        entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("description", getDeliveryInfoFirst().getDescription(), getDeliveryInfoLast().getDescription());
        assertFalse(valueChangeList.contains(entry));
    }

    @Test
    @DisplayName("Test comparing delivery info entities by default comparator")
    public void test_entities_by_defaultComparator() {
        // given
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), is(greaterThanOrEqualTo(0)));
        assertThat(valueChangeList.size(), Matchers.is(lessThanOrEqualTo(ReflectionUtils.getAllFields(DeliveryInfo.class).length)));

        assertTrue(valueChangeList.stream().noneMatch(value -> Objects.equals(value.getFirst(), value.getLast())));
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included properties and custom gid field comparator (with non equal gid)")
    public void test_entitiesWithNonEqualGidFields_by_customComparator() {
        // given
        final List<String> includedProperties = Arrays.asList("id", "type", "createdAt", "description");
        getDeliveryInfoFirst().setDescription(null);
        getDeliveryInfoLast().setDescription(null);

        final DefaultDiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);
        diffComparator.includeProperties(includedProperties);
        diffComparator.setComparator("gid", (Comparator<String>) (o1, o2) -> o1.substring(0, 5).compareToIgnoreCase(o2.substring(0, 5)));

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), IsEqual.equalTo(3));

        DefaultDiffEntry entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());

        entry = DefaultDiffEntry.of("gid", getDeliveryInfoFirst().getGid(), getDeliveryInfoLast().getGid());
        assertFalse(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getGid(), getDeliveryInfoLast().getGid());

        entry = DefaultDiffEntry.of("updatedAt", getDeliveryInfoFirst().getUpdatedAt(), getDeliveryInfoLast().getUpdatedAt());
        assertFalse(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getUpdatedAt(), getDeliveryInfoLast().getUpdatedAt());

        entry = DefaultDiffEntry.of("description", getDeliveryInfoFirst().getDescription(), getDeliveryInfoLast().getDescription());
        assertFalse(valueChangeList.contains(entry));
        assertEquals(getDeliveryInfoFirst().getDescription(), getDeliveryInfoLast().getDescription());
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included properties and custom gid field comparator (with equals gid)")
    public void test_entitiesWithEqualsGidFields_by_defaultComparator() {
        // given
        final List<String> includedProperties = Arrays.asList("id", "type", "gid", "createdAt");
        final String DEFAULT_GID_PREFIX = "TEST_";
        getDeliveryInfoFirst().setGid(DEFAULT_GID_PREFIX + UUID.randomUUID().toString());
        getDeliveryInfoLast().setGid(DEFAULT_GID_PREFIX + UUID.randomUUID().toString());

        final DefaultDiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);
        diffComparator.includeProperties(includedProperties);
        diffComparator.setComparator("gid", (Comparator<String>) (o1, o2) -> o1.substring(0, 5).compareToIgnoreCase(o2.substring(0, 5)));

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), IsEqual.equalTo(3));

        DefaultDiffEntry entry = DefaultDiffEntry.of("updatedAt", getDeliveryInfoFirst().getUpdatedAt(), getDeliveryInfoLast().getUpdatedAt());
        assertFalse(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getUpdatedAt(), getDeliveryInfoLast().getUpdatedAt());

        entry = DefaultDiffEntry.of("gid", getDeliveryInfoFirst().getGid(), getDeliveryInfoLast().getGid());
        assertFalse(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getGid(), getDeliveryInfoLast().getGid());

        entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertTrue(valueChangeList.contains(entry));
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included properties and custom created/updated fields comparator")
    public void test_entityDateFields_by_customComparators() {
        // given
        final List<String> includedProperties = Arrays.asList("createdAt", "updatedAt");
        getDeliveryInfoFirst().setCreatedAt(toDate("07/06/2013 12:13:14", DEFAULT_DATETIME_FORMAT));
        getDeliveryInfoFirst().setUpdatedAt(toDate("17/06/2018 14:13:12", DEFAULT_DATETIME_FORMAT));
        getDeliveryInfoLast().setCreatedAt(toDate("01/05/2013 15:01:01", DEFAULT_DATETIME_FORMAT));
        getDeliveryInfoLast().setUpdatedAt(toDate("17/07/2018 16:17:17", DEFAULT_DATETIME_FORMAT));

        final DefaultDiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);
        diffComparator.includeProperties(includedProperties);
        diffComparator.setComparator("createdAt", Comparator.comparingInt((Date d) -> LocalDateTime.fromDateFields(d).getHourOfDay()));
        diffComparator.setComparator("updatedAt", Comparator.comparingInt((Date d) -> LocalDateTime.fromDateFields(d).getDayOfMonth()));

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), IsEqual.equalTo(1));

        DefaultDiffEntry entry = DefaultDiffEntry.of("createdAt", getDeliveryInfoFirst().getCreatedAt(), getDeliveryInfoLast().getCreatedAt());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getCreatedAt(), getDeliveryInfoLast().getCreatedAt());

        entry = DefaultDiffEntry.of("updatedAt", getDeliveryInfoFirst().getUpdatedAt(), getDeliveryInfoLast().getUpdatedAt());
        assertFalse(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getUpdatedAt(), getDeliveryInfoLast().getUpdatedAt());
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included createdAt property and custom field comparator")
    public void test_entityCreatedAtField_by_customComparator() {
        // given
        final int DEFAULT_DIFFERENCE_DELTA = 24 * 60 * 60 * 1000;
        final List<String> includedProperties = Arrays.asList("createdAt");

        final DefaultDiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);
        diffComparator.includeProperties(includedProperties);
        diffComparator.setComparator("createdAt", (Comparator<Date>) (d1, d2) -> Math.abs(d1.getTime() - d2.getTime()) <= DEFAULT_DIFFERENCE_DELTA ? 0 : d1.compareTo(d2));

        final LocalDate initialDate = DateUtils.now();
        getDeliveryInfoFirst().setCreatedAt(toDate(initialDate));
        getDeliveryInfoLast().setCreatedAt(toDate(initialDate.plus(1, ChronoUnit.DAYS)));

        // when
        Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(empty()));

        getDeliveryInfoFirst().setCreatedAt(toDate(initialDate));
        getDeliveryInfoLast().setCreatedAt(toDate(initialDate.minus(2, ChronoUnit.DAYS)));

        // when
        iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList.size(), IsEqual.equalTo(1));
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included balance property and custom field comparator")
    public void test_entityBalanceField_by_customComparator() {
        // given
        final double DEFAULT_DIFFERENCE_DELTA = 0.0001;
        final List<String> includedProperties = Arrays.asList("balance");

        final DefaultDiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);
        diffComparator.includeProperties(includedProperties);
        diffComparator.setComparator("balance", (Comparator<Double>) (d1, d2) -> Math.abs(d1 - d2) <= DEFAULT_DIFFERENCE_DELTA ? 0 : d1.compareTo(d2));

        getDeliveryInfoFirst().setBalance(1.0000567);
        getDeliveryInfoLast().setBalance(1.0000547);

        // when
        Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(empty()));

        getDeliveryInfoFirst().setBalance(1.0000547);
        getDeliveryInfoLast().setBalance(1.0001567);

        // when
        iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList.size(), IsEqual.equalTo(1));
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included non-equal addresses property and custom comparator")
    public void test_entitiesWithNonEqualAddressFields_by_defaultComparator() {
        // given
        final List<String> includedProperties = Arrays.asList("addresses");
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class, DEFAULT_DELIVERY_INFO_COMPARATOR, includedProperties, Collections.emptyList());

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), is(greaterThanOrEqualTo(0)));
        assertThat(valueChangeList.size(), is(lessThanOrEqualTo(getDeliveryInfoFirst().getAddresses().size())));

        DefaultDiffEntry entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("addresses", getDeliveryInfoFirst().getAddresses(), getDeliveryInfoLast().getAddresses());
        assertTrue(valueChangeList.contains(entry));
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included equal addresses property and custom comparator")
    public void test_entitiesWithEqualAddressFields_by_defaultComparator() {
        // given
        final List<String> includedProperties = Arrays.asList("addresses");
        final DiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class, DEFAULT_DELIVERY_INFO_COMPARATOR, includedProperties, Collections.emptyList());

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoFirst());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(empty()));
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included properties and default array comparator")
    public void test_entitiesWithIncludedProperties_by_defaultArrayComparator() {
        // given
        final List<String> includedProperties = Arrays.asList("id", "codes", "description");
        final DefaultDiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);
        diffComparator.includeProperties(includedProperties);

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), is(greaterThanOrEqualTo(0)));
        assertThat(valueChangeList.size(), is(lessThanOrEqualTo(includedProperties.size())));

        DefaultDiffEntry entry = DefaultDiffEntry.of("createdAt", getDeliveryInfoFirst().getCreatedAt(), getDeliveryInfoLast().getCreatedAt());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("updatedAt", getDeliveryInfoFirst().getUpdatedAt(), getDeliveryInfoLast().getUpdatedAt());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());

        entry = DefaultDiffEntry.of("codes", getDeliveryInfoFirst().getCodes(), getDeliveryInfoLast().getCodes());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getCodes(), getDeliveryInfoLast().getCodes());
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included properties and default array comparator")
    public void test_enttitiesWithIncludedProperties_by_customArrayComparator() {
        // given
        final Integer[] testCodes = {10, 20, 30, 50, 70};
        final List<String> includedProperties = Arrays.asList("id", "codes", "description");
        getDeliveryInfoFirst().setCodes(testCodes);
        getDeliveryInfoLast().setCodes(testCodes);

        final DefaultDiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);
        diffComparator.includeProperties(includedProperties);
        diffComparator.setComparator("codes", new ComparatorUtils.LexicographicalNullSafeIntArrayComparator());

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), is(greaterThanOrEqualTo(0)));
        assertThat(valueChangeList.size(), is(lessThanOrEqualTo(includedProperties.size())));

        DefaultDiffEntry entry = DefaultDiffEntry.of("createdAt", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());

        entry = DefaultDiffEntry.of("codes", getDeliveryInfoFirst().getCodes(), getDeliveryInfoLast().getCodes());
        assertFalse(valueChangeList.contains(entry));
        assertArrayEquals(getDeliveryInfoFirst().getCodes(), getDeliveryInfoLast().getCodes());
    }

    @Test
    @DisplayName("Test comparing delivery info entities by included properties and default iterable comparator")
    public void test_entitiesWithIncludedProperties_by_customIterableComparator() {
        // given
        final List<AddressInfo> addressInfoList = Arrays.asList(getAddressInfoMock().val(), getAddressInfoMock().val());
        final List<String> includedProperties = Arrays.asList("id", "addresses", "description");
        getDeliveryInfoFirst().setAddresses(addressInfoList);
        getDeliveryInfoLast().setAddresses(addressInfoList);

        final DefaultDiffComparator<DeliveryInfo> diffComparator = DefaultDiffComparatorFactory.create(DeliveryInfo.class);
        diffComparator.includeProperties(includedProperties);
        diffComparator.setComparator("addresses", new ComparatorUtils.DefaultNullSafeIterableComparator<>(
            Comparator.comparing(AddressInfo::getId)
                .thenComparing(AddressInfo::getCity)
                .thenComparing(AddressInfo::getCountry)));

        // when
        final Iterable<DefaultDiffEntry> iterable = diffComparator.diffCompare(getDeliveryInfoFirst(), getDeliveryInfoLast());
        assertNotNull("Collection of difference entries should negate be null", iterable);
        final List<DefaultDiffEntry> valueChangeList = Lists.newArrayList(iterable);

        // then
        assertThat(valueChangeList, is(not(empty())));
        assertThat(valueChangeList.size(), is(greaterThanOrEqualTo(0)));
        assertThat(valueChangeList.size(), is(lessThanOrEqualTo(includedProperties.size())));

        DefaultDiffEntry entry = DefaultDiffEntry.of("createdAt", getDeliveryInfoFirst().getCreatedAt(), getDeliveryInfoLast().getCreatedAt());
        assertFalse(valueChangeList.contains(entry));

        entry = DefaultDiffEntry.of("id", getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());
        assertTrue(valueChangeList.contains(entry));
        assertNotEquals(getDeliveryInfoFirst().getId(), getDeliveryInfoLast().getId());

        entry = DefaultDiffEntry.of("addresses", getDeliveryInfoFirst().getAddresses(), getDeliveryInfoLast().getAddresses());
        assertFalse(valueChangeList.contains(entry));
        assertEquals(getDeliveryInfoFirst().getAddresses(), getDeliveryInfoLast().getAddresses());
    }
}
