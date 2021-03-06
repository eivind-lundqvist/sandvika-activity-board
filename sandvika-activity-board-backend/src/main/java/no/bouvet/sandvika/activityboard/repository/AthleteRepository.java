package no.bouvet.sandvika.activityboard.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import no.bouvet.sandvika.activityboard.domain.Athlete;

@RepositoryRestResource(collectionResourceRel = "athlete", path = "athlete")
public interface AthleteRepository extends MongoRepository<Athlete, String>
{
    public Athlete findByLastName(String lastName);

}
