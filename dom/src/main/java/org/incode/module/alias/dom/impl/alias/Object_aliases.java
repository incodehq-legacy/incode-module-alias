/*
 *
 *  Copyright 2015 incode.org
 *
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
package org.incode.module.alias.dom.impl.alias;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.Contributed;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.applib.annotation.RenderType;
import org.apache.isis.applib.annotation.SemanticsOf;

import org.incode.module.alias.dom.AliasModule;

@Mixin
public class Object_aliases {

    //region  > (injected)
    @Inject
    AliasRepository aliasRepository;
    //endregion

    //region > constructor
    private final Object aliased;
    public Object_aliases(final Object aliased) {
        this.aliased = aliased;
    }

    public Object getAliased() {
        return aliased;
    }
    //endregion

    //region > $$

    public static class DomainEvent extends AliasModule.ActionDomainEvent<Object_aliases> { } { }
    @Action(
            domainEvent = DomainEvent.class,
            semantics = SemanticsOf.SAFE
    )
    @ActionLayout(
            contributed = Contributed.AS_ASSOCIATION
    )
    @CollectionLayout(
            named = "Aliases", // regression in isis 1.11.x requires this to be specified
            render = RenderType.EAGERLY
    )
    public List<Alias> $$() {
        return aliasRepository.findByAliased(this.aliased);
    }

    public boolean hide$$() {
        return !aliasRepository.supports(this.aliased);
    }
    //endregion


}