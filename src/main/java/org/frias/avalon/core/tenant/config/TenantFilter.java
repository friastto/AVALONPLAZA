package org.frias.avalon.core.tenant.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.frias.avalon.core.jwt.util.SecurityUtils;
import org.frias.avalon.core.tenant.tenantcontex.TenantContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


public class TenantFilter  {

}