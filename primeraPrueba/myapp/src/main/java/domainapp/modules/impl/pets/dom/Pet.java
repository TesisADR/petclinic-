/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
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
package domainapp.modules.impl.pets.dom;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.VersionStrategy;

import com.google.common.collect.ComparisonChain;

import org.joda.time.LocalDateTime;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Auditing;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Parameter;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.repository.RepositoryService;

import domainapp.modules.impl.visits.dom.Visit;
import lombok.Getter;
import lombok.Setter;

@javax.jdo.annotations.PersistenceCapable(identityType = IdentityType.DATASTORE, schema = "pets" )
@javax.jdo.annotations.DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = "id")
@javax.jdo.annotations.Version(strategy= VersionStrategy.DATE_TIME, column ="version")
@javax.jdo.annotations.Unique(name="Pet_owner_name_UNQ", members = {"owner","name"})
@DomainObject(auditing = Auditing.ENABLED)
@DomainObjectLayout()  // causes UI events to be triggered
public class Pet implements Comparable<Pet> {

    public Pet(final Owner owner, final String name, final PetSpecies petSpecies) {
        this.owner = owner;
        this.name = name;
        this.petSpecies = petSpecies;
    }

    public String title() {
        return String.format(
                "%s (%s owned by %s)",
                getName(), getPetSpecies().name().toLowerCase(), getOwner().getName());
    }

    @javax.jdo.annotations.Column(allowsNull = "false", name = "ownerId")
    @Property(editing = Editing.DISABLED)
    @Getter @Setter
    private Owner owner;

    @javax.jdo.annotations.Column(allowsNull = "false", length = 40)
    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    private String name;

    @javax.jdo.annotations.Column(allowsNull = "false")
    @Property(editing = Editing.DISABLED)
    @Getter @Setter
    private PetSpecies petSpecies;

    @javax.jdo.annotations.Column(allowsNull = "true", length = 4000)
    @Property(editing = Editing.ENABLED)
    @Getter @Setter
    private String notes;

    @Override
    public String toString() {
        return getName();
    }

    @Action(semantics = SemanticsOf.NON_IDEMPOTENT)
    public Visit bookVisit(
            final LocalDateTime at,
            @Parameter(maxLength = 4000)
            @ParameterLayout(multiLine = 5)
            final String reason) {
        return repositoryService.persist(new Visit(this, at, reason));
    }

    public LocalDateTime default0BookVisit() {
        return clockService.now()
                .plusDays(1)
                .toDateTimeAtStartOfDay()
                .toLocalDateTime()
                .plusHours(9);
    }



    public String validate0BookVisit(final LocalDateTime proposed) {
        return proposed.isBefore(clockService.nowAsLocalDateTime())
                ? "Capo, no podes agendar un dia en el pasado"
                : null;
    }

    @Override
    public int compareTo(final Pet other) {
        return ComparisonChain.start()
                .compare(this.getOwner(), other.getOwner())
                .compare(this.getName(), other.getName())
                .result();
    }

    @javax.jdo.annotations.NotPersistent
    @javax.inject.Inject
    RepositoryService repositoryService;

    @javax.jdo.annotations.NotPersistent
    @javax.inject.Inject
    ClockService clockService;


}
