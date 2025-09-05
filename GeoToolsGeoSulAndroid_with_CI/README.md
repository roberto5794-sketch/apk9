
# GeoTools GeoSul — Vosk Offline STT + MLKit OCR

Este projeto usa **Vosk** para reconhecimento de voz 100% offline e **ML Kit** para OCR.

## Como usar o modelo Vosk (pt-BR)

1. Baixe um modelo pt-BR do Vosk (por exemplo: `vosk-model-small-pt-0.3`).
2. Coloque os arquivos do modelo dentro da pasta:
   `app/src/main/assets/models/pt`
   (ex.: `app/src/main/assets/models/pt/am`, `conf`, `graph`, `rescore` etc. conforme o pacote).
3. Abra no Android Studio, **Sync** e **Build APK**.

> O código copia o modelo de `assets/models/pt` para o armazenamento interno na primeira execução.

## JS Bridge
- `Android.scanText()` → abre câmera e faz OCR (ML Kit).
- `Android.startListening()` / `stopListening()` → inicia/para Vosk (offline).

## Observações
- O TTS não está incluído nesta variante; se quiser, adicionamos `TextToSpeech` nativo.
- Para OCR mais robusto, podemos trocar a Intent de câmera por **CameraX**.
