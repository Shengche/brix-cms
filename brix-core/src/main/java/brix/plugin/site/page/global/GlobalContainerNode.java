package brix.plugin.site.page.global;

import javax.jcr.Node;
import javax.jcr.Session;

import brix.Brix;
import brix.jcr.JcrNodeWrapperFactory;
import brix.jcr.RepositoryUtil;
import brix.jcr.api.JcrNode;
import brix.jcr.api.JcrSession;
import brix.jcr.wrapper.BrixFileNode;
import brix.jcr.wrapper.BrixNode;
import brix.plugin.site.page.AbstractContainer;

public class GlobalContainerNode extends AbstractContainer
{
	public static final String TYPE = Brix.NS_PREFIX + "globalContainer";
	
    public static JcrNodeWrapperFactory FACTORY = new JcrNodeWrapperFactory()
    {
        @Override
        public boolean canWrap(Brix brix, JcrNode node)
        {
            return TYPE.equals(getNodeType(node));
        }

        @Override
        public JcrNode wrap(Brix brix, Node node, JcrSession session)
        {
            return new GlobalContainerNode(node, session);
        }

        @Override
        public void initializeRepository(Brix brix, Session session)
        {
            RepositoryUtil.registerNodeType(session.getWorkspace(), TYPE,
                false, false, true);
        }		
    };

    public GlobalContainerNode(Node delegate, JcrSession session)
    {
        super(delegate, session);
    }

    public static GlobalContainerNode initialize(JcrNode node)
    {
        BrixNode brixNode = (BrixNode)node;
        BrixFileNode.initialize(node, "text/html");
        brixNode.setNodeType(TYPE);

        return new GlobalContainerNode(node.getDelegate(), node.getSession());
    }
    
    @Override
    public String getUserVisibleName()
    {
    	return "Global Tiles and Variables";
    }
}
