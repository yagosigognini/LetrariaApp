package br.com.letrariaapp.ui.features.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// ADICIONE O IMPORT PARA O NOSSO COMPONENTE DE FUNDO
import br.com.letrariaapp.ui.components.AppBackground

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: (String, String, String) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // TROCAMOS O 'Box' COM COR SÓLIDA PELO NOSSO COMPONENTE DE IMAGEM DE FUNDO
    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Criar conta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextColor)

            Spacer(modifier = Modifier.height(48.dp))

            // Campo de Nome
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TextColor,
                    unfocusedBorderColor = TextColor.copy(alpha = 0.5f),
                    focusedLabelColor = TextColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TextColor,
                    unfocusedBorderColor = TextColor.copy(alpha = 0.5f),
                    focusedLabelColor = TextColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de Senha
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TextColor,
                    unfocusedBorderColor = TextColor.copy(alpha = 0.5f),
                    focusedLabelColor = TextColor
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botão de Cadastrar
            Button(
                onClick = { onRegisterClick(nome, email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ButtonRed)
            ) {
                Text("CADASTRAR", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Botão de Texto para Login
            TextButton(onClick = onLoginClick) {
                Text("Já tem uma conta? Faça login", color = TextColor)
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(onLoginClick = {}, onRegisterClick = { _, _, _ -> })
}