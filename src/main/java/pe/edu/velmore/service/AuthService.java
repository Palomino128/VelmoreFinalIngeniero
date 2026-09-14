package pe.edu.velmore.service;

import pe.edu.velmore.dto.LoginDto;
import pe.edu.velmore.model.Usuario;

public interface AuthService {
    Usuario autenticar(LoginDto login);
}
