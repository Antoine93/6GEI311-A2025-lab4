## Leçons apprises - Lab 4 API REST

  ### Configuration et démarrage

  - Classpath Windows vs Linux : Sous Windows, le séparateur de classpath est ; (pas :). Erreur classique lors du portage de commandes Unix.
  - Dépendances externes : Gson nécessaire pour sérialisation JSON. Les erreurs IDE (soulignements rouges) n'impactent pas la compilation/exécution si le classpath est correct.

  ### Architecture REST

  - Route racine optionnelle : Une API REST n'a pas besoin de handler pour /api/v1, mais c'est une bonne pratique pour fournir une page d'accueil (infos, version, status).
  - Navigation navigateur limitée : Le navigateur ne teste que les routes GET. Pour POST/PATCH/DELETE, il faut curl ou Postman.

  ### Bonnes pratiques identifiées

  - Documentation proactive : Maintenir le README.md et plan-travail-lab4.md à jour facilite le suivi et la reprise du projet.
  - Endpoints découvrables : Lister les endpoints disponibles au démarrage du serveur améliore l'expérience développeur.
