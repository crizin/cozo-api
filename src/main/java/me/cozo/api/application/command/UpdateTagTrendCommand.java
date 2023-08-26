package me.cozo.api.application.command;

import me.cozo.api.domain.model.TagTrend;

import java.time.LocalDate;
import java.util.List;

public record UpdateTagTrendCommand(LocalDate date, List<TagTrend> tagTrends) {
}
