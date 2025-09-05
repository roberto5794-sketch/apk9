CLOUD BUILD (GitHub Actions) — Como gerar o APK sem instalar nada no PC

1) Crie um repositório novo no GitHub (privado ou público).
2) Faça upload do conteúdo DESTA pasta (inclui .github/workflows/build-apk.yml).
   - Dica: no GitHub, botão "Add file" → "Upload files".
3) Abra a aba "Actions" do repositório e habilite Workflows se for pedido.
4) Rode o workflow "Android CI - Build APK" (botão "Run workflow").
5) Ao terminar, clique no job e baixe os artefatos:
   - GeoTools-GeoSul-APK-Release → app-release.apk
   - GeoTools-GeoSul-APK-Debug   → app-debug.apk (fallback)

Observações:
- O projeto baixa automaticamente o modelo Vosk pt-BR no build.
- Usa JDK 17 e SDK 34. Ajuste em .github/workflows/build-apk.yml se precisar.
- Para mudar o nome do app/ícone, edite app/src/main/AndroidManifest.xml e assets/www.
