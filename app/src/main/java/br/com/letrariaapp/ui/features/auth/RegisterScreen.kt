package br.com.letrariaapp.ui.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.letrariaapp.R
// ADICIONE O IMPORT PARA O NOSSO COMPONENTE DE FUNDO
import br.com.letrariaapp.ui.components.AppBackground
import androidx.compose.foundation.clickable // <-- Novo import
import androidx.compose.foundation.text.ClickableText // <-- Novo import
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun RegisterScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: (String, String, String) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    // Variável de estado para o checkbox
    var isChecked by remember { mutableStateOf(false) }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_letraria),
                contentDescription = "Logo Letraria",
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ... (Todos os OutlinedTextFields de nome, email e senha, sem alteração) ...

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

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Repetir a Senha") },
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

                Spacer(modifier = Modifier.height(16.dp))

                // --- ADICIONADO: Checkbox e Texto dos Termos ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = TextColor,
                            uncheckedColor = TextColor
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Li e concordo com as políticas e termos",
                        color = TextColor,
                        modifier = Modifier.clickable {
                            // Ação para mostrar o pop-up dos termos virá aqui
                            println("Clicou nos termos!")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Espaço antes do botão principal

                // Botão de Cadastrar (agora desabilitado se os termos não forem aceitos)
                Button(
                    onClick = { onRegisterClick(nome, email, password) },
                    enabled = isChecked, // <-- O botão só funciona se isChecked for true
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonRed)
                ) {
                    Text("CADASTRAR", color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onLoginClick) {
                    Text("Já tem uma conta? Faça login", color = TextColor)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    RegisterScreen(onLoginClick = {}, onRegisterClick = { _, _, _ -> })
}