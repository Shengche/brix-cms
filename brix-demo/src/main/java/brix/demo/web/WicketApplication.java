package brix.demo.web;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Session;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.request.IRequestCycleProcessor;
import org.apache.wicket.request.target.coding.HybridUrlCodingStrategy;

import brix.Brix;
import brix.Path;
import brix.config.BrixConfig;
import brix.config.PrefixUriMapper;
import brix.config.UriMapper;
import brix.demo.web.admin.AdminPage;
import brix.jcr.JcrSessionFactory;
import brix.jcr.api.JcrSession;
import brix.plugin.site.SitePlugin;
import brix.web.BrixRequestCycleProcessor;
import brix.web.nodepage.ForbiddenPage;
import brix.web.nodepage.ResourceNotFoundPage;
import brix.workspace.Workspace;
import brix.workspace.WorkspaceManager;

/**
 * Application object for your web application. If you want to run this application without
 * deploying, run the Start class.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public final class WicketApplication extends AbstractWicketApplication
{

    /** brix instance */
    private Brix brix;

    /** {@inheritDoc} */
    @Override
    protected IRequestCycleProcessor newRequestCycleProcessor()
    {
        /*
         * install brix request cycle processor
         * 
         * this will allow brix to take over part of wicket's url space and handle requests
         */
        return new BrixRequestCycleProcessor(brix);
    }

    /** {@inheritDoc} */
    @Override
    protected void init()
    {
        super.init();

        final JcrSessionFactory sf = getJcrSessionFactory();
        final WorkspaceManager wm = getWorkspaceManager();

        try
        {
            // create uri mapper for the cms
            // we are mounting the cms on the root, and getting the workspace name from the
            // application properties
            UriMapper mapper = new PrefixUriMapper(Path.ROOT)
            {
                public Workspace getWorkspaceForRequest(WebRequestCycle requestCycle, Brix brix)
                {
                    final String name = getProperties().getJcrDefaultWorkspace();
                    SitePlugin sitePlugin = SitePlugin.get(brix);
                    return sitePlugin.getSiteWorkspace(name, "");
                }
            };

            // create brix configuration
            BrixConfig config = new BrixConfig(sf, wm, mapper);
            config.setHttpPort(getProperties().getHttpPort());
            config.setHttpsPort(getProperties().getHttpsPort());

            // create brix instance and attach it to this application
            brix = new DemoBrix(config);
            brix.attachTo(this);
            initializeRepository();
            initDefaultWorkspace();
        }
        finally
        {
            cleanupSessionFactory();
        }

        // mount admin page
        mount(new HybridUrlCodingStrategy("/admin", AdminPage.class)
        {
            @SuppressWarnings("unchecked")
            @Override
            protected IRequestTarget handleExpiredPage(String pageMapName, Class pageClass,
                    int trailingSlashesCount, boolean redirect)
            {
                return new HybridBookmarkablePageRequestTarget(pageMapName, (Class)pageClassRef
                    .get(), null, trailingSlashesCount, redirect);
            }
        });

        mountBookmarkablePage("/NotFound", ResourceNotFoundPage.class);
        mountBookmarkablePage("/Forbiden", ForbiddenPage.class);
    }

    private void initDefaultWorkspace()
    {
        try
        {
            final String defaultState = "";
            final String wn = getProperties().getJcrDefaultWorkspace();
            final SitePlugin sp = SitePlugin.get(brix);


            if (!sp.siteExists(wn, defaultState))
            {
                Workspace w = sp.createSite(wn, defaultState);
                JcrSession session = brix.getCurrentSession(w.getId());

                session.importXML("/", getClass().getResourceAsStream("workspace.xml"),
                    ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);

                session.save();
            }

        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not initialize jackrabbit workspace with Brix", e);
        }
    }

    private void initializeRepository()
    {
        try
        {
            Session session = brix.getCurrentSession(null);
            brix.initRepository(session);
            session.save();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Couldn't initialize jackrabbit repository", e);
        }
    }


}
