// Em ui/components/AppBackground.kt
package br.com.letrariaapp.ui.components // Verifique se o nome do pacote está correto

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import br.com.letrariaapp.R

@Composable
fun AppBackground(
    // Este parâmetro permite colocar qualquer conteúdo "dentro" do nosso fundo
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Imagem de fundo que preenche toda a tela
        Image(
            painter = painterResource(id = R.drawable.app_background),
            contentDescription = null, // Decorativo, não precisa de descrição
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Garante que a imagem cubra a tela sem distorcer
        )

        // Aqui o conteúdo da sua tela (Login, Home, etc.) será renderizado
        content()
    }
}