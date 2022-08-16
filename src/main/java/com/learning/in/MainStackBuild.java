package com.learning.in;


import com.amazonaws.util.StringUtils;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awscdk.core.App;
import software.amazon.awscdk.core.Environment;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.core.Tags;

public final class MainStackBuild {
    public static final Args ARGS = new Args();
    public static void main(final String[] args) {
        JCommander.newBuilder().addObject(ARGS).build().parse(args);
        App app  = new App();
        final String stackName = ARGS.getPrefixedName("stack");
        addTags(app,stackName);
        new CreateStack(app,stackName, StackProps.builder()
                .env(Environment.builder()
                        .account(ARGS.getAccountId())
                        .region(ARGS.getRegion())
                        .build()).build(),ARGS);
        app.synth();

    }

    public static void addTags(App app, final String stackName) {
        Tags.of(app).add("myId",ARGS.myId);
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Args{
        @Parameter(
                names = {"-program"},
                description = "Required : Program",
                required = true
        )
        public String program;
        @Parameter(
                names = {"-myId"},
                description = "Required : tag myId",
                required = true
        )
        public String myId;
        @Parameter(
                names = {"-accountId"},
                description = "Optional : tag accountId",
                required = true
        )
        public String accountId;
        @Parameter(
                names = {"-region"},
                description = "Optional : tag region",
                required = true
        )
        public String region;

        public String getPrefixedName(final String name) {
            if (StringUtils.isNullOrEmpty(name)) {
                return String.format("%s", this.program,"-");
            }else{
                return String.format("%s",this.program,"-",name);
            }
        }
    }
}
