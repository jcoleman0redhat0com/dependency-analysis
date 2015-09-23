package org.jboss.da.reports.backend.api;

import java.util.Optional;
import org.apache.maven.scm.ScmException;
import org.jboss.da.communication.CommunicationException;
import org.jboss.da.communication.aprox.model.GAVDependencyTree;
import org.jboss.da.communication.model.GAV;
import org.jboss.da.communication.pom.PomAnalysisException;
import org.jboss.da.reports.api.SCMLocator;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
public interface DependencyTreeGenerator {

    public GAVDependencyTree getDependencyTree(SCMLocator scml) throws ScmException,
            PomAnalysisException, CommunicationException;

    public Optional<GAVDependencyTree> getDependencyTree(GAV gav) throws CommunicationException;

    public GAVToplevelDependencies getToplevelDependencies(SCMLocator scml) throws ScmException,
            PomAnalysisException, CommunicationException;

    public Optional<GAVToplevelDependencies> getToplevelDependencies(GAV gav)
            throws CommunicationException;

}
