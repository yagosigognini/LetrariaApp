package br.com.letrariaapp.ui.features.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import br.com.letrariaapp.R
import br.com.letrariaapp.ui.components.AppBackground
import br.com.letrariaapp.ui.theme.LetrariaAppTheme
import br.com.letrariaapp.ui.theme.ButtonRed
import br.com.letrariaapp.ui.theme.TextColor

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onLoginClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val authResult by viewModel.authResult.observeAsState(AuthResult.IDLE)
    val context = LocalContext.current

    LaunchedEffect(authResult) {
        when (val result = authResult) {
            is AuthResult.SUCCESS -> {
                Toast.makeText(context, "Cadastro realizado! Verifique seu e-mail para continuar.", Toast.LENGTH_LONG).show()
                onRegisterSuccess()
                viewModel.resetAuthResult()
            }
            is AuthResult.ERROR -> {
                Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                viewModel.resetAuthResult()
            }
            else -> {}
        }
    }

    if (showTermsDialog) {
        TermsDialog(onDismiss = { showTermsDialog = false })
    }

    RegisterScreenContent(
        nome = nome,
        onNomeChange = { nome = it },
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        confirmPassword = confirmPassword,
        onConfirmPasswordChange = { confirmPassword = it },
        isChecked = isChecked,
        onCheckedChange = { isChecked = it },
        onRegisterClick = {
            if (password != confirmPassword) {
                Toast.makeText(context, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.register(nome, email, password)
            }
        },
        onLoginClick = onLoginClick,
        onTermsClick = { showTermsDialog = true },
        isLoading = authResult is AuthResult.LOADING
    )
}

@Composable
fun RegisterScreenContent(
    nome: String, onNomeChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmPasswordChange: (String) -> Unit,
    isChecked: Boolean, onCheckedChange: (Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onTermsClick: () -> Unit,
    isLoading: Boolean
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    AppBackground(backgroundResId = R.drawable.app_background) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_letraria),
                    contentDescription = "Logo Letraria",
                    modifier = Modifier.size(180.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(value = nome, onValueChange = onNomeChange, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, enabled = !isLoading)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true, enabled = !isLoading)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password, onValueChange = onPasswordChange, label = { Text("Senha") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, null) }
                        },
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPassword, onValueChange = onConfirmPasswordChange, label = { Text("Repetir a Senha") },
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(image, null) }
                        },
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = isChecked, onCheckedChange = onCheckedChange, enabled = !isLoading)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Li e concordo com as políticas e termos",
                            color = TextColor,
                            modifier = Modifier.clickable(onClick = onTermsClick)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRegisterClick,
                        enabled = isChecked && !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ButtonRed)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text("CADASTRAR", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(onClick = onLoginClick, enabled = !isLoading) {
                        Text("Já tem uma conta? Faça login", color = TextColor)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun TermsDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Políticas e Termos", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Aqui vai o texto completo dos seus termos de serviço e políticas de privacidade...")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("FECHAR")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    LetrariaAppTheme {
        AppBackground(R.drawable.app_background) {
            RegisterScreenContent(
                nome = "", onNomeChange = {},
                email = "", onEmailChange = {},
                password = "", onPasswordChange = {},
                confirmPassword = "", onConfirmPasswordChange = {},
                isChecked = false, onCheckedChange = {},
                onRegisterClick = {}, onLoginClick = {}, onTermsClick = {},
                isLoading = false
            )
        }
    }
}