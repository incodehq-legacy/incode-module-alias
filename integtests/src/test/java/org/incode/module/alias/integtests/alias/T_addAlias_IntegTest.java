/*
 *  Copyright 2014~2015 Dan Haywood
 *
 *  Licensed under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.incode.module.alias.integtests.alias;

import com.google.common.eventbus.Subscribe;
import org.apache.isis.applib.AbstractSubscriber;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.incode.module.alias.dom.impl.Alias;
import org.incode.module.alias.dom.impl.T_addAlias;
import org.incode.module.alias.dom.spi.AliasType;
import org.incode.module.alias.dom.spi.AliasTypeRepository;
import org.incode.module.alias.dom.spi.ApplicationTenancyRepository;
import org.incode.module.alias.fixture.dom.demo.DemoObjectMenu;
import org.incode.module.alias.fixture.scripts.teardown.AliasDemoObjectsTearDownFixture;
import org.incode.module.alias.integtests.AliasModuleIntegTest;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class T_addAlias_IntegTest extends AliasModuleIntegTest {

    @Inject
    DemoObjectMenu demoObjectMenu;

    @Inject
    AliasTypeRepository aliasTypeRepository;

    @Inject
    ApplicationTenancyRepository applicationTenancyRepository;

    Object aliased;

    @Before
    public void setUpData() throws Exception {
        fixtureScripts.runFixtureScript(new AliasDemoObjectsTearDownFixture(), null);

        aliased = wrap(demoObjectMenu).create("Foo");
    }

    public static class ActionImplementationIntegTest extends T_addAlias_IntegTest {

        @Before
        public void setUp() throws Exception {
            assertThat(wrap(mixinAliases(aliased)).$$()).isEmpty();
        }

        @Test
        public void can_add_alias() throws Exception {

            // when
            final Collection<String> atPaths = applicationTenancyRepository.atPathsFor(aliased);
            final String randomAtPath = fakeData.collections().anyOf(atPaths);

            final Collection<AliasType> aliasTypes = aliasTypeRepository.aliasTypesFor(aliased, randomAtPath);
            final AliasType randomAliasType = fakeData.collections().anyOf(aliasTypes);
            final String randomAliasRef = fakeData.strings().digits(10);

            wrap(mixinAddAlias(aliased)).$$(randomAtPath, randomAliasType, randomAliasRef);

            // then
            final List<Alias> aliases = wrap(mixinAliases(aliased)).$$();
            assertThat(aliases).hasSize(1);
        }

        @Test
        public void can_add_to_same_ref_to_same_atPath_and_different_aliasTypes() throws Exception {

            // when
            final Collection<String> atPaths = applicationTenancyRepository.atPathsFor(aliased);
            final String randomAtPath = fakeData.collections().anyOf(atPaths);

            final Collection<AliasType> aliasTypes = aliasTypeRepository.aliasTypesFor(aliased, randomAtPath);
            final AliasType randomAliasType = fakeData.collections().anyOf(aliasTypes);
            final AliasType randomAliasType2 = fakeData.collections().anyOfExcept(
                    aliasTypes,
                    aliasType -> Objects.equals(aliasType.getId(), randomAliasType.getId()));
            final String randomAliasRef = fakeData.strings().digits(10);

            wrap(mixinAddAlias(aliased)).$$(randomAtPath, randomAliasType, randomAliasRef);
            wrap(mixinAddAlias(aliased)).$$(randomAtPath, randomAliasType2, randomAliasRef);

            // then
            final List<Alias> aliases = wrap(mixinAliases(aliased)).$$();
            assertThat(aliases).hasSize(2);
        }

        @Test
        public void can_add_to_same_ref_to_different_atPaths_and_same_aliasType() throws Exception {

            // when
            final Collection<String> atPaths = applicationTenancyRepository.atPathsFor(aliased);
            final String randomAtPath = fakeData.collections().anyOf(atPaths);
            final String randomAtPath2 = fakeData.collections().anyOfExcept(
                    atPaths, atPath -> Objects.equals(atPath, randomAtPath));

            final Collection<AliasType> aliasTypes = aliasTypeRepository.aliasTypesFor(aliased, randomAtPath);
            final AliasType randomAliasType = fakeData.collections().anyOf(aliasTypes);
            final String randomAliasRef = fakeData.strings().digits(10);

            wrap(mixinAddAlias(aliased)).$$(randomAtPath, randomAliasType, randomAliasRef);
            wrap(mixinAddAlias(aliased)).$$(randomAtPath2, randomAliasType, randomAliasRef);

            // then
            final List<Alias> aliases = wrap(mixinAliases(aliased)).$$();
            assertThat(aliases).hasSize(2);
        }

        @Test
        public void cannot_add_to_same_ref_to_same_atPath_and_same_aliasType() throws Exception {

            expectedException.expectCause(of(SQLIntegrityConstraintViolationException.class));

            // given
            final Collection<String> atPaths = applicationTenancyRepository.atPathsFor(aliased);
            final String randomAtPath = fakeData.collections().anyOf(atPaths);

            final Collection<AliasType> aliasTypes = aliasTypeRepository.aliasTypesFor(aliased, randomAtPath);
            final AliasType randomAliasType = fakeData.collections().anyOf(aliasTypes);
            final String randomAliasRef = fakeData.strings().digits(10);

            wrap(mixinAddAlias(aliased)).$$(randomAtPath, randomAliasType, randomAliasRef);

            // when
            wrap(mixinAddAlias(aliased)).$$(randomAtPath, randomAliasType, randomAliasRef);

            // then
            final List<Alias> aliases = wrap(mixinAliases(aliased)).$$();
            assertThat(aliases).isEmpty();
        }

        @Test
        public void cannot_add_to_different_ref_to_same_atPath_and_same_aliasTypes() throws Exception {

            expectedException.expectCause(of(SQLIntegrityConstraintViolationException.class));

            // when
            final Collection<String> atPaths = applicationTenancyRepository.atPathsFor(aliased);
            final String randomAtPath = fakeData.collections().anyOf(atPaths);

            final Collection<AliasType> aliasTypes = aliasTypeRepository.aliasTypesFor(aliased, randomAtPath);
            final AliasType randomAliasType = fakeData.collections().anyOf(aliasTypes);
            final String randomAliasRef = fakeData.strings().digits(10);
            final String randomAliasRef2 = fakeData.strings().digits(10);

            wrap(mixinAddAlias(aliased)).$$(randomAtPath, randomAliasType, randomAliasRef);
            wrap(mixinAddAlias(aliased)).$$(randomAtPath, randomAliasType, randomAliasRef2);

            // then
            final List<Alias> aliases = wrap(mixinAliases(aliased)).$$();
            assertThat(aliases).hasSize(2);
        }

    }


    public static class DomainEventIntegTest extends T_addAlias_IntegTest {

        @DomainService(nature = NatureOfService.DOMAIN)
        public static class Subscriber extends AbstractSubscriber {

            T_addAlias.DomainEvent ev;

            @Subscribe
            public void on(T_addAlias.DomainEvent ev) {
                this.ev = ev;
            }
        }

        @Inject
        Subscriber subscriber;

        @Test
        public void fires_event() throws Exception {

            // given
            assertThat(wrap(mixinAliases(aliased)).$$()).isEmpty();

            // when
            final Collection<String> atPaths = applicationTenancyRepository.atPathsFor(aliased);
            final String randomAtPath = fakeData.collections().anyOf(atPaths);

            final Collection<AliasType> aliasTypes = aliasTypeRepository.aliasTypesFor(aliased, randomAtPath);
            final AliasType randomAliasType = fakeData.collections().anyOf(aliasTypes);
            final String randomAliasRef = fakeData.strings().fixed(10);

            final T_addAlias mixinAddAlias = mixinAddAlias(aliased);
            wrap(mixinAddAlias).$$(randomAtPath, randomAliasType, randomAliasRef);

            // then
            assertThat(subscriber.ev).isNotNull();

            // the following is no longer true (ISIS-1425); the wrapper factory dereferences the mixin to invoke
            // the mixed-in action on the domain object.  The net result is we get a new instance of the mixin as
            // the source of the event.
            // assertThat(subscriber.ev.getSource()).isSameAs(mixinAddAlias);

            assertThat(subscriber.ev.getSource().getAliased()).isSameAs(aliased);
            assertThat(subscriber.ev.getArguments().get(0)).isEqualTo(randomAtPath);
            assertThat(subscriber.ev.getArguments().get(1)).isEqualTo(randomAliasType);
            assertThat(subscriber.ev.getArguments().get(2)).isEqualTo(randomAliasRef);
        }
    }

}