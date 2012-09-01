/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.plugins.buildcomparison.outcome.internal.archive.entry

import static org.gradle.api.plugins.buildcomparison.compare.internal.ComparisonResultType.*

import org.gradle.api.plugins.buildcomparison.outcome.internal.archive.GeneratedArchiveBuildOutcomeComparisonResult
import org.gradle.api.plugins.buildcomparison.render.internal.html.BuildOutcomeComparisonResultHtmlRenderer
import org.gradle.api.plugins.buildcomparison.render.internal.html.HtmlRenderContext

/*
    TODO - missing test coverage
 */
class GeneratedArchiveBuildOutcomeComparisonResultHtmlRenderer extends BuildOutcomeComparisonResultHtmlRenderer<GeneratedArchiveBuildOutcomeComparisonResult> {

    GeneratedArchiveBuildOutcomeComparisonResultHtmlRenderer(String fromSideName, String toSideName) {
        super(fromSideName, toSideName)
    }

    Class<GeneratedArchiveBuildOutcomeComparisonResult> getResultType() {
        return GeneratedArchiveBuildOutcomeComparisonResult.class;
    }

    void render(GeneratedArchiveBuildOutcomeComparisonResult result, HtmlRenderContext context) {
        renderTitle(result, context)

        def from = result.compared.from
        def to = result.compared.to

        context.render {
            h4 "Details"
            table {
                tr {
                    th class: "border-right", ""
                    th "Generated Location (relative)"
                    th "Copied Location"
                }
                tr {
                    th class: "border-right no-border-bottom", fromSideName
                    td from.rootRelativePath
                    td from.archiveFile.absolutePath
                }
                tr {
                    th class: "border-right no-border-bottom", toSideName
                    td to.rootRelativePath
                    td to.archiveFile.absolutePath
                }
            }
        }

        context.render { h4 "Comparison Results" }

        if (result.comparisonResultType == NON_EXISTENT) {
            context.render { p "Neither side produced the archive." }
        } else if (result.comparisonResultType == FROM_ONLY) {
            context.render { p "The archive was only produced by the $fromSideName." }
        } else if (result.comparisonResultType == TO_ONLY) {
            context.render { p "The archive was only produced on the $toSideName." }
        } else if (result.comparisonResultType == EQUAL) {
            context.render { p "The archives are completely identical." }
        } else if (result.comparisonResultType == UNEQUAL) {
            renderUnequal(context, result.entryComparisons)
        } else {
            result.comparisonResultType.throwUnsupported()
        }
    }

    private void renderUnequal(HtmlRenderContext context, Iterable<ArchiveEntryComparison> entryComparisons) {
        context.render {
            table {
                tr {
                    th "Path"
                    th "Difference"
                }

                entryComparisons.each { entryComparison ->
                    if (entryComparison.comparisonResultType != EQUAL) {
                        tr {
                            td entryComparison.path
                            td toDifferenceDescription(entryComparison)
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("GroovyMissingReturnStatement")
    protected String toDifferenceDescription(ArchiveEntryComparison entryComparison) {
        switch (entryComparison.comparisonResultType) {
            case FROM_ONLY:
                "Only exists in $fromSideName"
                break
            case TO_ONLY:
                "Only exists in $toSideName"
                break
            case UNEQUAL:
                toDifferenceDescription(entryComparison.from, entryComparison.to)
                break
            default:
                entryComparison.comparisonResultType.throwUnsupported()
        }
    }

    protected String toDifferenceDescription(ArchiveEntry from, ArchiveEntry to) {
        if (from.directory != to.directory) {
            if (from.directory) {
                "entry is a directory in the $fromSideName and a file in the $toSideName"
            } else {
                "entry is a directory in the $toSideName and a file in the $fromSideName"
            }
        } else if (from.size != to.size) {
            "entry in the $fromSideName is $from.size bytes - in the $toSideName it is $to.size bytes (${formatSizeDiff(to.size - from.size)})"
        } else if (from.crc != to.crc) {
            "entries are of identical size but have different content"
        } else {
            throw new IllegalStateException("Method was called with equal entries")
        }
    }

    protected String formatSizeDiff(Long diff) {
        (diff > 0 ? "+" : "") + diff.toString()
    }

}
