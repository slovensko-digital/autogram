package digital.slovensko.autogram.server.dto;

public record PresentationParameters(ServerSigningParameters.VisualizationWidthEnum visualizationWidth) {
	public int getVisualizationWidth() {
		if (visualizationWidth == null)
			return 0;

		return switch (visualizationWidth) {
			case sm -> 640;
			case md -> 768;
			case lg -> 1024;
			case xl -> 1280;
			case xxl -> 1536;
		};
	}
}