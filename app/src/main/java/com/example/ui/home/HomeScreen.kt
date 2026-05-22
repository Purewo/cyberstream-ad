package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.api.HomepageData
import com.example.data.api.MovieSimple

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
  val uiState by viewModel.uiState.collectAsState()
  val scrollState = rememberScrollState()

  Box(modifier = Modifier.fillMaxSize()) {
    when (val state = uiState) {
      is HomeUiState.Loading -> {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
      }
      is HomeUiState.Error -> {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = state.message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { viewModel.fetchHomepage() }) {
                Text("重试")
            }
        }
      }
      is HomeUiState.Success -> {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
        ) {
          // Top Hero Poster
          state.data.hero?.movie?.let { heroMovie ->
              HeroPoster(heroMovie)
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Content area
          state.data.sections?.forEach { section ->
              if (!section.items.isNullOrEmpty()) {
                  Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onBackground
                  )

                  LazyRow(
                      contentPadding = PaddingValues(horizontal = 16.dp),
                      horizontalArrangement = Arrangement.spacedBy(16.dp),
                      modifier = Modifier.fillMaxWidth()
                  ) {
                      items(section.items) { movie ->
                          MovieCard(movie = movie)
                      }
                  }
                  
                  Spacer(modifier = Modifier.height(16.dp))
              }
          }
          
          Spacer(modifier = Modifier.height(32.dp))
        }
      }
    }
  }
}

private fun getImageUrl(assetUrl: String?, url: String?): String? {
    if (assetUrl != null) {
        if (assetUrl.startsWith("http")) return assetUrl
        else return "https://cyberstream.ma1.gameuniverse.top${if (assetUrl.startsWith("/")) "" else "/"}$assetUrl"
    }
    return url
}

@Composable
fun HeroPoster(movie: MovieSimple) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(400.dp)
  ) {
    AsyncImage(
      model = getImageUrl(movie.posterAssetUrl, movie.posterUrl),
      contentDescription = "Hero Poster",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize()
    )

    // Bottom gradient to blend into background
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color.Transparent,
              MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
              MaterialTheme.colorScheme.background
            ),
            startY = 400f
          )
        )
    )

    // Text overlay on poster
    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(16.dp)
    ) {
      Text(
        text = movie.title,
        style = MaterialTheme.typography.headlineLarge,
        color = Color.White
      )
      val details = mutableListOf<String>()
      if (movie.year != null && movie.year > 0) details.add(movie.year.toString())
      if (movie.qualityBadge != null) details.add(movie.qualityBadge)
      if (movie.rating != null && movie.rating > 0) details.add("★ ${movie.rating}")
      
      Text(
        text = details.joinToString(" • "),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.7f)
      )
    }
  }
}

@Composable
fun MovieCard(movie: MovieSimple) {
    Column(modifier = Modifier.width(120.dp)) {
        Card(
            modifier = Modifier
                .width(120.dp)
                .height(180.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = getImageUrl(movie.posterAssetUrl, movie.posterUrl),
                    contentDescription = movie.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                if (movie.qualityBadge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = movie.qualityBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        
        if (movie.year != null && movie.year > 0) {
            Text(
                text = movie.year.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }
}
