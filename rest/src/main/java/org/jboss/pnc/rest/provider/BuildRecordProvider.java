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
package org.jboss.pnc.rest.provider;

import static org.jboss.pnc.datastore.predicates.BuildRecordPredicates.*;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

@Stateless
public class BuildRecordProvider {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildRecordRepository buildRecordRepository;
    private BuildCoordinator buildCoordinator;

    public BuildRecordProvider() {
    }

    @Inject
    public BuildRecordProvider(BuildRecordRepository buildRecordRepository, BuildCoordinator buildCoordinator) {
        this.buildRecordRepository = buildRecordRepository;
        this.buildCoordinator = buildCoordinator;
    }

    public List<BuildRecordRest> getAllArchived(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildRecord.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(buildRecordRepository.findAll(filteringCriteria.get(), paging))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildRecordRest> getAllRunning(Integer pageIndex, Integer pageSize, String sortingRsql, String rsql) {
        if(!Strings.isNullOrEmpty(sortingRsql)) {
            logger.warn("Sorting RSQL is not supported, ignoring");
        }

        if(!Strings.isNullOrEmpty(rsql)) {
            logger.warn("Querying RSQL is not supported, ignoring");
        }

        return nullableStreamOf(buildCoordinator.getBuildTasks()).map(submittedBuild -> new BuildRecordRest(submittedBuild))
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
    }

    public List<BuildRecordRest> getAllArchivedOfBuildConfiguration(int pageIndex, int pageSize, String sortingRsql, String query, Integer buildRecordId) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildRecord.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(buildRecordRepository.findAll(withBuildRecordId(buildRecordId).and(filteringCriteria.get()), paging))
                .map(buildRecord -> new BuildRecordRest(buildRecord))
                .collect(Collectors.toList());
    }

    public List<BuildRecordRest> getAllForBuildConfiguration(int pageIndex, int pageSize, String sortingRsql, String query, Integer configurationId) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildRecord.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(buildRecordRepository.findAll(withBuildConfigurationId(configurationId).and(filteringCriteria.get()), paging))
                .map(buildRecord -> new BuildRecordRest(buildRecord))
                .collect(Collectors.toList());
    }

    public List<BuildRecordRest> getAllForProject(int pageIndex, int pageSize, String sortingRsql, String query, Integer projectId) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildRecord.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(buildRecordRepository.findAll(withProjectId(projectId).and(filteringCriteria.get()), paging))
                .map(buildRecord -> new BuildRecordRest(buildRecord))
                .collect(Collectors.toList());
    }

    public BuildRecordRest getSpecific(Integer id) {
        BuildRecord buildRecord = buildRecordRepository.findOne(id);
        if (buildRecord != null) {
            return new BuildRecordRest(buildRecord);
        }
        return null;
    }
    

    public String getBuildRecordLog(Integer id) {
        BuildRecord buildRecord = buildRecordRepository.findOne(id);
        if(buildRecord != null)
            return buildRecord.getBuildLog();
        else 
            return null;
    }
    
    public StreamingOutput getLogsForBuild(String buildRecordLog) {
        if(buildRecordLog == null)
            return null;
        
        return outputStream -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(buildRecordLog);
            writer.flush();
        };
    }

    public BuildRecordRest getSpecificRunning(Integer id) {
        BuildTask buildTask = getSubmittedBuild(id);
        if (buildTask != null) {
            return new BuildRecordRest(buildTask);
        }
        return null;
    }

    private BuildTask getSubmittedBuild(Integer id) {
        List<BuildTask> buildTasks = buildCoordinator.getBuildTasks().stream()
                .filter(submittedBuild -> id.equals(submittedBuild.getBuildConfiguration().getId()))
                .collect(Collectors.toList());
        if (!buildTasks.isEmpty()) {
            return buildTasks.iterator().next();
        }
        return null;
    }

    public String getSubmittedBuildLog(Integer id) {
        BuildTask buildTask = getSubmittedBuild(id);
        if (buildTask != null ) 
            return buildTask.getBuildLog();
        else
            return null;
    }

    public StreamingOutput getStreamingOutputForString(String str) {
        if(str == null)
            return null;
            
        return outputStream -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(str);
            writer.flush();
        };
    }
    public Function<? super BuildRecord, ? extends BuildRecordRest> toRestModel() {
        return buildRecord -> new BuildRecordRest(buildRecord);
    }
}