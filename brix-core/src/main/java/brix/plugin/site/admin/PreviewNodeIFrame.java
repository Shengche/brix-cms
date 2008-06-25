package brix.plugin.site.admin;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;

import brix.Brix;
import brix.jcr.wrapper.BrixNode;
import brix.web.BrixRequestCycleProcessor;
import brix.web.nodepage.BrixNodeRequestTarget;
import brix.web.nodepage.BrixPageParameters;

public class PreviewNodeIFrame extends WebMarkupContainer<BrixNode>
{

    private static final String PREVIEW_PARAM = Brix.NS_PREFIX + "preview";

    public static boolean isPreview()
    {
        BrixPageParameters params = BrixPageParameters.getCurrent();
        if (params != null)
        {
            if (params.getQueryParam(PREVIEW_PARAM).toBoolean(false))
            {
                return true;
            }
        }
        return false;
    }

    public PreviewNodeIFrame(String id, IModel<BrixNode> model)
    {
        super(id, model);
        setOutputMarkupId(true);
    }

    @Override
    protected void onComponentTag(ComponentTag tag)
    {
        super.onComponentTag(tag);
        tag.put("src", getUrl());
    }

    private CharSequence getUrl()
    {
        BrixPageParameters parameters = new BrixPageParameters();
        IModel<BrixNode> nodeModel = getModel();
        String workspace = nodeModel.getObject().getSession().getWorkspace().getName();
        parameters.setQueryParam(BrixRequestCycleProcessor.WORKSPACE_PARAM, workspace);
        StringBuilder url = new StringBuilder(getRequestCycle().urlFor(
            new BrixNodeRequestTarget(nodeModel, parameters)));

        if (url.indexOf("?") >= 0)
        {
            url.append("&");
        }
        else
        {
            url.append("?");
        }
        url.append(PREVIEW_PARAM).append("=true");

        return url;
    }
}
