package org.lenskit.mooc.nonpers.assoc;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.basic.AbstractItemBasedItemRecommender;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.*;

/**
 * An item-based item scorer that uses association rules.
 */
public class AssociationItemBasedItemRecommender extends AbstractItemBasedItemRecommender {
    private static final Logger logger = LoggerFactory.getLogger(AssociationItemBasedItemRecommender.class);
    private final AssociationModel model;

    /**
     * Construct the item scorer.
     *
     * @param m The association rule model.
     */
    @Inject
    public AssociationItemBasedItemRecommender(AssociationModel m) {
        model = m;
    }

    @Override
    public ResultList recommendRelatedItemsWithDetails(Set<Long> basket, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        LongSet items;
        if (candidates == null) {
            items = model.getKnownItems();
        } else {
            items = LongUtils.asLongSet(candidates);
        }

        if (exclude != null) {
            items = LongUtils.setDifference(items, LongUtils.asLongSet(exclude));
        }

        if (basket.isEmpty()) {
            return Results.newResultList();
        } else if (basket.size() > 1) {
            logger.warn("Reference set has more than 1 item, picking arbitrarily.");
        }

        long refItem = basket.iterator().next();

        return recommendItems(n, refItem, items);
    }

    /**
     * Recommend items with an association rule.
     * @param n The number of recommendations to produce.
     * @param refItem The reference item.
     * @param candidates The candidate items (set of items that can possibly be recommended).
     * @return The list of results.
     */
    private ResultList recommendItems(int n, long refItem, LongSet candidates) {
        List<Result> results = new ArrayList<>();
        refItem = 2761;
        // TODO Compute the n highest-scoring items from candidates
        Iterator itr = candidates.iterator();
        while (itr.hasNext()){
            long Id = (long)itr.next();
            if(model.hasItem(Id)){
                double associationScore = model.getItemAssociation(refItem, Id);
                results.add(Results.create(Id, associationScore));
            }
        }
        if(n < 0){
            return Results.newResultList(results);
        }else {
            Comparator<Result> comparator = new Comparator<Result>() {
                @Override
                public int compare(Result o1, Result o2) {
                    if (o1.getScore() > o2.getScore()) {
                        return -1;
                    } else if (o1.getScore() == o2.getScore()) {
                        return 0;
                    } else {
                        return 1;
                    }
                }
            };
            //results.sort(comparator);
            Collections.sort(results, comparator);
            return Results.newResultList(results.subList(0,n));
        }
    }
}
