package com.citasmedicas.appcitasmedicas.security;



import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.citasmedicas.appcitasmedicas.Service.UserDetailsServiceImpl;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain
    ) throws ServletException, IOException {

        // 1. Rutas públicas — dejar pasar sin revisar token
        String path = request.getServletPath();
        if (path.startsWith("/api/auth")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Leer el header Authorization
        String authHeader = request.getHeader("Authorization");

        // 3. Si no hay token o no empieza con "Bearer ", rechazar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // 4. Extraer el token (quitar el prefijo "Bearer ")
        String token = authHeader.substring(7);

        // 5. Extraer el username (email) del token
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (Exception e) {
            // Token malformado o con firma inválida
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Token inválido\"}");
            return;
        }

        // 6. Si hay username y el contexto no tiene autenticación aún
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 7. Cargar el usuario desde la base de datos
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 8. Validar que el token no esté expirado y pertenezca a este usuario
            if (jwtService.isTokenValid(token, userDetails)) {

                // 9. Crear el objeto de autenticación y meterlo en el contexto de Security
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);

            } else {
                // Token expirado o inválido
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Token expirado\"}");
                return;
            }
        }

        // 10. Continuar con el siguiente filtro
        chain.doFilter(request, response);
    }
}
