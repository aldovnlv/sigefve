// Decodificar token JWT sin usar librerías externas
export const decodeToken = (token) => {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(
            atob(base64)
                .split('')
                .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                .join('')
        );
        return JSON.parse(jsonPayload);
    } catch (error) {
        console.error('Error al decodificar token:', error);
        return null;
    }
};

// Verificar si el token ha expirado
export const isTokenExpired = (token) => {
    const decoded = decodeToken(token);
    if (!decoded || !decoded.exp) return true;

    const currentTime = Date.now() / 1000;
    return decoded.exp < currentTime;
};

// Extraer información del usuario desde el token
export const getUserFromToken = (token) => {
    const decoded = decodeToken(token);
    if (!decoded) return null;

    return {
        id: decoded.id_usuario,
        username: decoded.usuario,
        rol: decoded.rol
    };
};
