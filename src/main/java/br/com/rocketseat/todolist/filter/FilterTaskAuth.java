package br.com.rocketseat.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.BCrypt.Result;
import br.com.rocketseat.todolist.user.IUserRepository;
import br.com.rocketseat.todolist.user.UserModel;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    IUserRepository userRepository;

    @Autowired
    public FilterTaskAuth(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

                // Verifica a rota
                String servletPath = request.getServletPath();
                if (!servletPath.equals("/tasks/")) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // Pegar a autenticação (usuário e senha)
                String authorization = request.getHeader("Authorization");

                String authEncoded = authorization.substring("Basic".length()).trim();

                byte[] authDecode = Base64.getDecoder().decode(authEncoded);

                String authString = new String(authDecode);

                String[] credentials = authString.split(":");
                String username = credentials[0];
                String password = credentials[1];

                // Validar usuário
                UserModel user = userRepository.findByUsername(username);
                if (user == null) {
                    response.sendError(401);
                    return;
                }

                // Validar senha
                Result passwordVerify = BCrypt.verifyer().verify(password.toCharArray(),user.getPassword());
                if (!passwordVerify.verified) {
                    response.sendError(401);
                    return;
                }

                // Segue o fluxo normal da aplicação
                request.setAttribute("idUser", user.getId());
                filterChain.doFilter(request, response);
    }
}
