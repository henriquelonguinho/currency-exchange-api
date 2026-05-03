package com.currency.exchange.exchange.client.query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.web.util.UriBuilder;

/**
 * Builder for constructing query parameters for the Treasury Fiscal Data API.
 *
 * <p>Usage example:
 * <pre>{@code
 * FiscalDataQuery.builder()
 *     .filter("country_currency_desc", FilterOperator.EQ, "Brazil-Real")
 *     .sort("record_date", SortDirection.DESC)
 *     .fields("country_currency_desc", "exchange_rate", "record_date")
 *     .pageSize(1)
 *     .build();
 * }</pre>
 */
public class FiscalDataQuery {

    private final List<String> filters;
    private final List<String> sorts;
    private final List<String> fields;
    private final Integer pageSize;
    private final Integer pageNumber;

    private FiscalDataQuery(Builder builder) {
        this.filters = List.copyOf(builder.filters);
        this.sorts = List.copyOf(builder.sorts);
        this.fields = List.copyOf(builder.fields);
        this.pageSize = builder.pageSize;
        this.pageNumber = builder.pageNumber;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Consumer<UriBuilder> applyTo() {
        return uriBuilder -> {
            if (!filters.isEmpty()) {
                uriBuilder.queryParam("filter", String.join(",", filters));
            }
            if (!sorts.isEmpty()) {
                uriBuilder.queryParam("sort", String.join(",", sorts));
            }
            if (!fields.isEmpty()) {
                uriBuilder.queryParam("fields", String.join(",", fields));
            }
            if (pageNumber != null) {
                uriBuilder.queryParam("page[number]", pageNumber);
            }
            if (pageSize != null) {
                uriBuilder.queryParam("page[size]", pageSize);
            }
        };
    }

    public enum FilterOperator {
        EQ("eq"),
        LT("lt"),
        LTE("lte"),
        GT("gt"),
        GTE("gte"),
        IN("in");

        private final String value;

        FilterOperator(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum SortDirection {
        ASC(""),
        DESC("-");

        private final String prefix;

        SortDirection(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    public static class Builder {

        private final List<String> filters = new ArrayList<>();
        private final List<String> sorts = new ArrayList<>();
        private final List<String> fields = new ArrayList<>();
        private Integer pageSize;
        private Integer pageNumber;

        public Builder filter(String field, FilterOperator operator, String value) {
            filters.add(field + ":" + operator.getValue() + ":" + value);
            return this;
        }

        public Builder sort(String field, SortDirection direction) {
            sorts.add(direction.getPrefix() + field);
            return this;
        }

        public Builder fields(String... fieldNames) {
            fields.addAll(List.of(fieldNames));
            return this;
        }

        public Builder pageSize(int size) {
            this.pageSize = size;
            return this;
        }

        public Builder pageNumber(int number) {
            this.pageNumber = number;
            return this;
        }

        public FiscalDataQuery build() {
            return new FiscalDataQuery(this);
        }
    }
}
