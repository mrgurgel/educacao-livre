<%--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

--%>
<%--
  - Footer for home page
  --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ page contentType="text/html;charset=UTF-8" %>

<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.dspace.app.webui.util.UIUtil" %>

<%
    String sidebar = (String) request.getAttribute("dspace.layout.sidebar");
%>

            <%-- Right-hand side bar if appropriate --%>
<%
    if (sidebar != null)
    {
%>
	</div>
	<div class="col-md-3">
                    <%= sidebar %>
    </div>
    </div>       
<%
    }
%>
</div>
</main>


<div ui-view="footer" class="ng-scope"><footer class="ng-scope">
    <div class="container">
        <div class="col-xs-12 col-sm-12 col-md-8 col-lg-8">
            <ul class="sponsors-list">
                <li><img alt="" src="<%= request.getContextPath() %>/theme/thrive/image/logo-footer.svg"></li>
                <li><img alt="" src="<%= request.getContextPath() %>/theme/thrive/image/logo-unesco.svg"></li>
                <li><img alt="" src="<%= request.getContextPath() %>/theme/thrive/image/logo-oviin.svg"></li>
                <li><img alt="" src="<%= request.getContextPath() %>/theme/thrive/image/logo-sesi.svg"></li>
            </ul>
        </div>
        <div class="col-xs-12 col-sm-12 col-md-4 col-lg-4">
            <div class="social-label">Nossos Canais</div>
            <ul class="nav nav-pills social-list">
                <li role="presentation" class="facebook"><a ng-click="open()" href="javascript:void(0);"><i class="fa fa-facebook fa-2"></i></a></li>
                <li role="presentation" class="twitter"><a ng-click="open()" href="javascript:void(0);"><i class="fa fa-twitter fa-2"></i></a></li>
                <li role="presentation" class="youtube"><a ng-click="open()" href="javascript:void(0);"><i class="fa fa-youtube fa-2"></i></a></li>
                <li role="presentation" class="google-plus"><a ng-click="open()" href="javascript:void(0);"><i class="fa fa-google-plus fa-2"></i></a></li>
                <li role="presentation" class="instagram"><a ng-click="open()" href="javascript:void(0);"><i class="fa fa-instagram fa-2"></i></a></li>
            </ul>
        </div>
    </div>
</footer></div>

    </body>
</html>