/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.datastore.predicates;

import java.util.Collection;

import com.mysema.query.types.expr.BooleanExpression;

import org.jboss.pnc.model.QBuildRecord;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class BuildRecordPredicates {

    public static BooleanExpression withBuildRecordId(Integer buildRecordId) {
        return createNotNullPredicate(buildRecordId != null, () -> QBuildRecord.buildRecord.id.eq(buildRecordId));
    }

    public static BooleanExpression withBuildConfigurationId(Integer configurationId) {
        return createNotNullPredicate(configurationId != null, () -> QBuildRecord.buildRecord.buildConfigurationAudited.id.eq(configurationId));
    }
    
    public static BooleanExpression withBuildConfigurationIdInSet(Collection<Integer> buildConfigurationIds) {
        return createNotNullPredicate(buildConfigurationIds != null, () -> QBuildRecord.buildRecord.buildConfigurationAudited.id.in(buildConfigurationIds));
    }

    public static BooleanExpression withProjectId(Integer projectId) {
        return createNotNullPredicate(projectId != null, () -> QBuildRecord.buildRecord.buildConfigurationAudited.project.id.eq(projectId));
    }
}
